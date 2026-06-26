package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Text/Multimodal Data Classes ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String // Base64
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    val thinkingLevel: String // "HIGH", "LOW", etc.
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

// --- Veo Video Generation Data Classes ---

@JsonClass(generateAdapter = true)
data class GenerateVideosRequest(
    val prompt: String,
    val config: VeoConfig? = null
)

@JsonClass(generateAdapter = true)
data class VeoConfig(
    val numberOfVideos: Int,
    val resolution: String,
    val aspectRatio: String // "16:9" or "9:16"
)

@JsonClass(generateAdapter = true)
data class VeoOperationResponse(
    val name: String? = null,
    val done: Boolean? = null,
    val response: VeoResponseContent? = null,
    val error: VeoError? = null
)

@JsonClass(generateAdapter = true)
data class VeoResponseContent(
    @Json(name = "generatedVideos") val generatedVideos: List<GeneratedVideo>? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedVideo(
    val video: VideoContent? = null
)

@JsonClass(generateAdapter = true)
data class VideoContent(
    val uri: String? = null
)

@JsonClass(generateAdapter = true)
data class VeoError(
    val code: Int? = null,
    val message: String? = null
)

// --- Retrofit Api Service ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:generateVideos")
    suspend fun generateVideos(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateVideosRequest
    ): VeoOperationResponse

    @GET("v1beta/operations/{operationName}")
    suspend fun getOperation(
        @Path("operationName") operationName: String,
        @Query("key") apiKey: String
    ): VeoOperationResponse
}

// --- Retrofit Client Singleton ---

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
