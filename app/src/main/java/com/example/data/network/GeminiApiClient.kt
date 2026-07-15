package com.example.data.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

// --- Moshi Data Classes for Gemini REST API ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Retrofit Client ---

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateScript(
        topic: String,
        platform: String,
        tone: String,
        hookStyle: String,
        durationSeconds: Int,
        customPrompt: String
    ): String {
        // Retrieve API key safely
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getFallbackMockScript(topic, platform, tone, hookStyle, durationSeconds)
        }

        val prompt = """
            Write a highly engaging, high-conversion short-form video script about: "$topic".
            
            Format specifications:
            - Target Platform: $platform
            - Tone: $tone
            - Hook Framework / Style: $hookStyle
            - Max Duration: $durationSeconds seconds
            - Extra user instructions: "$customPrompt"
            
            Structure requirement:
            1. An attention-grabbing hook at the very start (0-5s).
            2. The core message / body (5-50s) split into clear visual/delivery parts.
            3. A strong Call-To-Action (CTA) at the end (50-60s).
            
            Write the output ready to be displayed on a teleprompter. 
            Include visual cues and director's prompts in brackets (e.g. [Cut to close up], [Show product], [Smile at camera]).
            Make sure the lines are short, easy to speak, and punchy. Avoid complex words. Use bullet points or visual pauses.
        """.trimIndent()

        val systemInstructionText = """
            You are a world-class content creator, viral YouTube Shorts & Instagram Reels scriptwriter, and digital marketing expert.
            Your writing is extremely punchy, conversational, and direct. You write for human speech.
            You use visual bracketed directions like [Show expression], [Zoom in] to help creators deliver the message.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemInstructionText))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Failed to generate script text from Gemini API response."
        } catch (e: Exception) {
            "API Call failed: ${e.localizedMessage}. Falling back to offline generator.\n\n" +
                    getFallbackMockScript(topic, platform, tone, hookStyle, durationSeconds)
        }
    }

    private fun getFallbackMockScript(
        topic: String,
        platform: String,
        tone: String,
        hookStyle: String,
        durationSeconds: Int
    ): String {
        return """
            [Hook - 0:00 - 0:05]
            [Point at camera with energy]
            "If you're still trying to create content for $platform about $topic without a structured hook... STOP what you are doing right now."
            
            [Body - 0:05 - 0:45]
            [Transition: Zoom in slightly, look serious]
            "Here is the brutal truth: Most creators fail because they write boring scripts. They don't use the '$hookStyle' framework.
            
            When your tone is too formal, people swipe away in 1.5 seconds.
            
            That’s why you need a three-step formula:
            
            [Gesture: Count on fingers]
            1. Pattern interrupt within the first split-second.
            2. Give them high value with zero fluff.
            3. Wrap it up with a clear action."
            
            [CTA - 0:45 - 0:60]
            [Smile warmly, hold up your phone]
            "Save this video right now so you don't forget this. Comment below with your biggest struggle, and hit follow for daily secrets!"
        """.trimIndent()
    }

    suspend fun generateAlternativeHooks(
        topic: String,
        platform: String,
        tone: String,
        hookStyle: String,
        scriptBody: String
    ): List<String> {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return listOf(
                "Want to master $topic? This 1 change is all it takes.",
                "STOP scrolling if you care about $topic right now.",
                "The shocking secret nobody tells you about $topic."
            )
        }

        val prompt = """
            You are a short-form video viral hook expert.
            Based on this script topic: "$topic" on $platform with tone "$tone" using style "$hookStyle", and the existing script:
            
            "$scriptBody"
            
            Generate exactly 3 alternative attention-grabbing hooks (0-5s) that are highly clickable and scroll-stopping.
            Format your response strictly as a bulleted list where each hook is on a new line starting with "Variant 1: ", "Variant 2: ", "Variant 3: ". Do not include extra introductions, markdown bold inside lines, or formatting. Just output the plain hook text.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.8f)
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseHooksFromText(responseText, topic)
        } catch (e: Exception) {
            listOf(
                "Want to master $topic? This 1 change is all it takes.",
                "STOP scrolling if you care about $topic right now.",
                "The shocking secret nobody tells you about $topic."
            )
        }
    }

    private fun parseHooksFromText(text: String, topic: String): List<String> {
        val lines = text.split("\n")
            .map { it.replace(Regex("^[-*#\\s•]+"), "").trim() }
            .filter { it.isNotBlank() }
        
        val list = mutableListOf<String>()
        for (line in lines) {
            val cleaned = line.replace(Regex("^(Variant \\d+:|\\d+\\.)"), "").trim().removeSurrounding("\"")
            if (cleaned.isNotBlank() && list.size < 3) {
                list.add(cleaned)
            }
        }
        while (list.size < 3) {
            list.add("The shocking truth about $topic you need to hear today!")
        }
        return list
    }

    suspend fun generateViralMetadata(
        topic: String,
        platform: String,
        tone: String,
        scriptText: String
    ): String {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return """
                📌 **VIRAL CAPTION**
                "Stop wasting time doing this wrong! Here is the exact framework to level up your $topic."
                
                🔥 **HIGH-CTR TITLES (Use on Cover)**
                1. The $platform hack you're ignoring!
                2. Do this for 10x better $topic
                3. The truth about $topic
                
                🏷️ **BEST SEO HASHTAGS**
                #${topic.replace(" ", "")} #creatorworkflow #growthhacks #viralshorts
            """.trimIndent()
        }

        val prompt = """
            You are a social media copywriter. Generate a complete metadata pack based on this short-form script:
            
            "$scriptText"
            
            Format the response exactly like this:
            📌 **VIRAL CAPTION**
            [A high-engagement, curiosity-inducing caption with hooky start, brief explanation, and call-to-action]
            
            🔥 **HIGH-CTR TITLES (Use on Cover)**
            1. [Title option 1 with high CTR under 50 chars]
            2. [Title option 2 with high CTR under 50 chars]
            3. [Title option 3 with high CTR under 50 chars]
            
            🏷️ **BEST SEO HASHTAGS**
            [5-8 high volume, niche relevant hashtags starting with #]
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Failed to generate metadata."
        } catch (e: Exception) {
            "Error generating metadata: ${e.localizedMessage}"
        }
    }

    suspend fun rewriteScriptTone(
        scriptText: String,
        originalTone: String,
        targetTone: String
    ): String {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "[Rewritten in $targetTone tone]\n\n" + scriptText.substringAfter("\n\n")
        }

        val prompt = """
            You are a master script re-writer. Take this existing script:
            
            "$scriptText"
            
            Rewrite the script to change the tone from "$originalTone" to a distinct "$targetTone" tone.
            Maintain the exact same topic, duration limit, platform format, and visual bracket cues [like this], but entirely change the delivery, punchlines, and energy to match the new tone "$targetTone".
            Output ONLY the rewritten script ready for a teleprompter, without any introduction or markdown code blocks.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Failed to rewrite script."
        } catch (e: Exception) {
            "[Rewritten in $targetTone tone]\n\n" + scriptText.substringAfter("\n\n" )
        }
    }
}
