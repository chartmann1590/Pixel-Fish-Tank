package com.charles.virtualpet.fishtank.data.feedback

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GithubApi {

    @Headers("Accept: application/vnd.github+json")
    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: CreateIssueRequest
    ): Response<GithubIssue>

    @Headers("Accept: application/vnd.github+json")
    @GET("repos/{owner}/{repo}/issues/{number}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): Response<GithubIssue>

    @Headers("Accept: application/vnd.github+json")
    @GET("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun getComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): Response<List<GithubComment>>

    @Headers("Accept: application/vnd.github+json")
    @POST("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun postComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int,
        @Body request: PostCommentRequest
    ): Response<GithubComment>

    @Headers("Accept: application/vnd.github+json")
    @PUT("repos/{owner}/{repo}/contents/{assetDir}/{filename}")
    suspend fun uploadAsset(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("assetDir") assetDir: String,
        @Path("filename") filename: String,
        @Body request: UploadAssetRequest
    ): Response<UploadAssetResponse>
}
