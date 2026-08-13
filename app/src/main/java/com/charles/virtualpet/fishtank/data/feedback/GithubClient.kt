package com.charles.virtualpet.fishtank.data.feedback

import com.charles.virtualpet.fishtank.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Talks to the cloudflare-worker/ feedback relay, not api.github.com directly. See
 * cloudflare-worker/src/index.ts, which holds the GitHub token server-side as a Worker
 * secret. Previously this embedded BuildConfig.GITHUB_API_TOKEN client-side as a Bearer
 * header, which shipped a real repo-write PAT in every release build (extractable from
 * the APK).
 */
object GithubClient {

    private const val BASE_URL = "https://pixel-fish-tank-github-feedback.charles-h-hartmann1.workers.dev/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
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

    val isConfigured: Boolean = true
}
