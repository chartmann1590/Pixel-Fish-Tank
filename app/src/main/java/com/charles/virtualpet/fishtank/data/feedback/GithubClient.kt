package com.charles.virtualpet.fishtank.data.feedback

import com.charles.virtualpet.fishtank.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object GithubClient {

    private const val BASE_URL = "https://api.github.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "Pixel-Fish-Tank-Android/1.0")

        if (BuildConfig.GITHUB_API_TOKEN.isNotEmpty()) {
            builder.header("Authorization", "Bearer ${BuildConfig.GITHUB_API_TOKEN}")
        }

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: GithubApi = retrofit.create(GithubApi::class.java)

    val isConfigured: Boolean
        get() = BuildConfig.GITHUB_API_TOKEN.isNotEmpty() &&
                BuildConfig.GITHUB_REPO_OWNER.isNotEmpty() &&
                BuildConfig.GITHUB_REPO_NAME.isNotEmpty()

    val owner: String get() = BuildConfig.GITHUB_REPO_OWNER
    val repo: String get() = BuildConfig.GITHUB_REPO_NAME
    val assetsDir: String get() = BuildConfig.FEEDBACK_ASSETS_DIR
}
