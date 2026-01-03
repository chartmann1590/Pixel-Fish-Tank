package com.charles.virtualpet.fishtank.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.charles.virtualpet.fishtank.domain.model.Decoration
import com.charles.virtualpet.fishtank.domain.model.DecorationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

private val Context.storeItemsDataStore: DataStore<Preferences> by preferencesDataStore(name = "store_items_cache")

@Serializable
data class StoreItemDto(
    val id: String = "",
    val name: String = "",
    val drawableRes: String = "",
    val imageUrl: String? = null,
    val price: Int = 0,
    val type: String = "",
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
) {
    // No-argument constructor for Firestore deserialization
    constructor() : this("", "", "", null, 0, "", true, System.currentTimeMillis(), 0)
}

class FirebaseStoreRepository(
    private val context: Context,
    private val imageCacheManager: ImageCacheManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val firestore = FirebaseFirestore.getInstance()
    private val storeItemsCollection = firestore.collection("store_items")
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _allFirebaseItems = MutableStateFlow<List<Decoration>>(emptyList())
    val allFirebaseItems: StateFlow<List<Decoration>> = _allFirebaseItems.asStateFlow()
    
    private val _hasMoreItems = MutableStateFlow(true)
    val hasMoreItems: StateFlow<Boolean> = _hasMoreItems.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private var lastDocumentSnapshot: QueryDocumentSnapshot? = null
    private val PAGE_SIZE = 20
    
    private object CacheKeys {
        val DECORATIONS_JSON = stringPreferencesKey("cached_decorations_json")
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
    }
    
    init {
        // Load cached items immediately in background
        scope.launch {
            loadCachedItems()
        }
    }
    
    /**
     * Gets all store items (hardcoded + Firebase) merged together
     */
    fun getAllStoreItems(): Flow<List<Decoration>> = flow {
        // Always emit hardcoded items first
        val hardcodedItems = DecorationStore.availableDecorations
        val firebaseItems = _allFirebaseItems.value
        
        // Merge: hardcoded items first, then Firebase items sorted by price
        val combined = (hardcodedItems + firebaseItems).sortedBy { it.price }
        emit(combined)
    }
    
    /**
     * Syncs store items from Firestore
     */
    suspend fun syncStoreItems(): Result<List<Decoration>> {
        _syncState.value = SyncState.Loading
        
        return try {
            // Reset pagination
            lastDocumentSnapshot = null
            _allFirebaseItems.value = emptyList()
            
            // Fetch first page
            val firstPage = fetchPage(null)
            val allItems = firstPage.toMutableList()
            
            // Continue fetching pages until no more items
            var currentLastDoc = lastDocumentSnapshot
            while (currentLastDoc != null && firstPage.size == PAGE_SIZE) {
                val nextPage = fetchPage(currentLastDoc)
                if (nextPage.isEmpty()) break
                allItems.addAll(nextPage)
                currentLastDoc = lastDocumentSnapshot
            }
            
            // Sort items client-side: by price (orderIndex sorting removed since we don't have it in query)
            val sortedItems = allItems.sortedBy { it.price }
            
            // Pre-cache images in background
            sortedItems.forEach { decoration ->
                decoration.imageUrl?.let { url ->
                    scope.launch {
                        imageCacheManager.getImage(url, decoration.id)
                    }
                }
            }
            
            // Update state
            _allFirebaseItems.value = sortedItems
            _hasMoreItems.value = false // All items loaded
            
            // Cache items
            cacheDecorations(sortedItems)
            
            val timestamp = System.currentTimeMillis()
            _syncState.value = SyncState.Success(timestamp)
            
            Result.success(sortedItems)
        } catch (e: Exception) {
            // If sync fails, return cached data
            val cached = getCachedDecorations()
            
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "No internet connection"
                e.message?.contains("permission", ignoreCase = true) == true -> 
                    "Unable to load store items"
                e.message?.contains("index", ignoreCase = true) == true ->
                    "Index required: ${e.message}"
                else -> "Failed to sync store items"
            }
            
            _syncState.value = SyncState.Error(errorMessage, e)
            
            if (cached.isNotEmpty()) {
                _allFirebaseItems.value = cached
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Fetches a page of items from Firestore
     * Simplified query to avoid composite index requirement - no orderBy in query
     */
    private suspend fun fetchPage(startAfter: QueryDocumentSnapshot?): List<Decoration> {
        // Simplified query: only filter by isActive, no orderBy to avoid composite index requirement
        // All sorting (orderIndex, price) will be done client-side
        var query = storeItemsCollection
            .whereEqualTo("isActive", true)
            .limit(PAGE_SIZE.toLong())
        
        if (startAfter != null) {
            query = query.startAfter(startAfter)
        }
        
        val snapshot = query.get().await()
        
        val items = snapshot.documents.mapNotNull { doc ->
            try {
                val dto = doc.toObject(StoreItemDto::class.java)
                
                dto?.let {
                    Decoration(
                        id = it.id,
                        name = it.name,
                        drawableRes = it.drawableRes,
                        imageUrl = it.imageUrl,
                        price = it.price,
                        type = DecorationType.valueOf(it.type)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        
        // Update last document for pagination
        if (snapshot.documents.isNotEmpty()) {
            lastDocumentSnapshot = snapshot.documents.last() as? QueryDocumentSnapshot
        }
        
        return items
    }
    
    /**
     * Loads more items (pagination)
     */
    suspend fun loadMoreItems(): Result<List<Decoration>> {
        if (!_hasMoreItems.value || _isLoadingMore.value) {
            return Result.success(emptyList())
        }
        
        _isLoadingMore.value = true
        
        return try {
            val startAfter = lastDocumentSnapshot
            if (startAfter == null) {
                // If no last document, we've loaded everything
                _hasMoreItems.value = false
                _isLoadingMore.value = false
                return Result.success(emptyList())
            }
            
            val nextPage = fetchPage(startAfter)
            
            if (nextPage.isEmpty()) {
                _hasMoreItems.value = false
            } else {
                val currentItems = _allFirebaseItems.value
                _allFirebaseItems.value = currentItems + nextPage
                
                // Pre-cache images
                nextPage.forEach { decoration ->
                    decoration.imageUrl?.let { url ->
                        scope.launch {
                            imageCacheManager.getImage(url, decoration.id)
                        }
                    }
                }
            }
            
            _isLoadingMore.value = false
            Result.success(nextPage)
        } catch (e: Exception) {
            _isLoadingMore.value = false
            Result.failure(e)
        }
    }
    
    /**
     * Gets cached decorations from DataStore
     */
    suspend fun getCachedDecorations(): List<Decoration> {
        return try {
            val json = context.storeItemsDataStore.data.map { prefs ->
                prefs[CacheKeys.DECORATIONS_JSON] ?: "[]"
            }.first()
            
            val dtos = Json.decodeFromString<List<StoreItemDto>>(json)
            dtos.map { dto ->
                Decoration(
                    id = dto.id,
                    name = dto.name,
                    drawableRes = dto.drawableRes,
                    imageUrl = dto.imageUrl,
                    price = dto.price,
                    type = DecorationType.valueOf(dto.type)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Loads cached items on init
     */
    private suspend fun loadCachedItems() {
        val cached = getCachedDecorations()
        if (cached.isNotEmpty()) {
            _allFirebaseItems.value = cached
        }
    }
    
    /**
     * Caches decorations locally
     */
    private suspend fun cacheDecorations(decorations: List<Decoration>) {
        context.storeItemsDataStore.edit { prefs ->
            val dtos = decorations.map { dec ->
                StoreItemDto(
                    id = dec.id,
                    name = dec.name,
                    drawableRes = dec.drawableRes,
                    imageUrl = dec.imageUrl,
                    price = dec.price,
                    type = dec.type.name,
                    isActive = true,
                    lastUpdated = System.currentTimeMillis(),
                    orderIndex = 0
                )
            }
            val json = Json.encodeToString(dtos)
            prefs[CacheKeys.DECORATIONS_JSON] = json
            prefs[CacheKeys.LAST_SYNC_TIME] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Gets last sync timestamp
     */
    suspend fun getLastSyncTimestamp(): Long? {
        return try {
            val timestampStr = context.storeItemsDataStore.data.map { prefs ->
                prefs[CacheKeys.LAST_SYNC_TIME]
            }.first()
            timestampStr?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Manual refresh trigger
     */
    suspend fun refresh() {
        syncStoreItems()
    }
    
    /**
     * Gets decoration by ID from Firebase items only (not hardcoded)
     */
    fun getDecorationById(id: String): Decoration? {
        // Check Firebase items directly (don't call DecorationStore to avoid recursion)
        return _allFirebaseItems.value.find { it.id == id }
    }
}

