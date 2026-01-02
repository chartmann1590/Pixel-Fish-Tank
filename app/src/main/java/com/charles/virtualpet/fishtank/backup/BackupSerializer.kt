package com.charles.virtualpet.fishtank.backup

import kotlinx.serialization.json.Json

object BackupSerializer {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    fun serialize(envelope: BackupEnvelope): String {
        return json.encodeToString(BackupEnvelope.serializer(), envelope)
    }
    
    fun deserialize(jsonString: String): BackupEnvelope {
        return json.decodeFromString(BackupEnvelope.serializer(), jsonString)
    }
}

