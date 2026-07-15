package com.example.data.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GroqMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class GroqResponse(
    val choices: List<GroqChoice>?
)

@JsonClass(generateAdapter = true)
data class GroqChoice(
    val index: Int,
    val message: GroqMessage?
)

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun generateChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqRequest
    ): GroqResponse
}

object GroqApiClient {
    private const val BASE_URL = "https://api.groq.com/"
    private const val DEFAULT_MODEL = "llama-3.3-70b-versatile"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GroqApiService::class.java)
    }

    suspend fun generateScript(
        topic: String,
        platform: String,
        tone: String,
        hookStyle: String,
        durationSeconds: Int,
        customPrompt: String
    ): String {
        val apiKey = try {
            BuildConfig.GROQ_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            // Fallback to Gemini if Groq key is not configured, or fallback to offline generator
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                return GeminiApiClient.generateScript(topic, platform, tone, hookStyle, durationSeconds, customPrompt)
            }
            return getFallbackMockScript(topic, platform, tone, hookStyle, durationSeconds)
        }

        val systemInstructionText = """
            You are a world-class content creator, viral YouTube Shorts & Instagram Reels scriptwriter, and digital marketing expert.
            Your writing is extremely punchy, conversational, and direct. You write for human speech.
            You use visual bracketed directions like [Show expression], [Zoom in] to help creators deliver the message.
        """.trimIndent()

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

        val request = GroqRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                GroqMessage(role = "system", content = systemInstructionText),
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.7f
        )

        return try {
            val response = apiService.generateChatCompletion("Bearer $apiKey", request)
            response.choices?.firstOrNull()?.message?.content
                ?: "Failed to generate script text from Groq API response."
        } catch (e: Exception) {
            // Fallback to Gemini if Groq API call fails
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                try {
                    return GeminiApiClient.generateScript(topic, platform, tone, hookStyle, durationSeconds, customPrompt)
                } catch (ex: Exception) {
                    // Ignore
                }
            }
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
        val apiKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                return GeminiApiClient.generateAlternativeHooks(topic, platform, tone, hookStyle, scriptBody)
            }
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

        val request = GroqRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.8f
        )

        return try {
            val response = apiService.generateChatCompletion("Bearer $apiKey", request)
            val responseText = response.choices?.firstOrNull()?.message?.content ?: ""
            parseHooksFromText(responseText, topic)
        } catch (e: Exception) {
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                try {
                    return GeminiApiClient.generateAlternativeHooks(topic, platform, tone, hookStyle, scriptBody)
                } catch (ex: Exception) {
                    // Ignore
                }
            }
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
        val apiKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                return GeminiApiClient.generateViralMetadata(topic, platform, tone, scriptText)
            }
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

        val request = GroqRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.7f
        )

        return try {
            val response = apiService.generateChatCompletion("Bearer $apiKey", request)
            response.choices?.firstOrNull()?.message?.content
                ?: "Failed to generate metadata."
        } catch (e: Exception) {
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                try {
                    return GeminiApiClient.generateViralMetadata(topic, platform, tone, scriptText)
                } catch (ex: Exception) {
                    // Ignore
                }
            }
            "Error generating metadata: ${e.localizedMessage}"
        }
    }

    suspend fun rewriteScriptTone(
        scriptText: String,
        originalTone: String,
        targetTone: String
    ): String {
        val apiKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                return GeminiApiClient.rewriteScriptTone(scriptText, originalTone, targetTone)
            }
            return "[Rewritten in $targetTone tone]\n\n" + scriptText.substringAfter("\n\n")
        }

        val prompt = """
            You are a master script re-writer. Take this existing script:
            
            "$scriptText"
            
            Rewrite the script to change the tone from "$originalTone" to a distinct "$targetTone" tone.
            Maintain the exact same topic, duration limit, platform format, and visual bracket cues [like this], but entirely change the delivery, punchlines, and energy to match the new tone "$targetTone".
            Output ONLY the rewritten script ready for a teleprompter, without any introduction or markdown code blocks.
        """.trimIndent()

        val request = GroqRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.7f
        )

        return try {
            val response = apiService.generateChatCompletion("Bearer $apiKey", request)
            response.choices?.firstOrNull()?.message?.content
                ?: "Failed to rewrite script."
        } catch (e: Exception) {
            val geminiKey = try { BuildConfig.GEMINI_API_KEY } catch (ex: Exception) { "" }
            if (geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY") {
                try {
                    return GeminiApiClient.rewriteScriptTone(scriptText, originalTone, targetTone)
                } catch (ex: Exception) {
                    // Ignore
                }
            }
            "[Rewritten in $targetTone tone]\n\n" + scriptText.substringAfter("\n\n" )
        }
    }

    suspend fun analyzeHook(
        hookText: String,
        topic: String,
        platform: String
    ): HookAnalysis {
        val apiKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            return getFallbackHookAnalysis(hookText, topic, platform)
        }

        val prompt = """
            You are a viral hook copywriter and conversion optimization expert.
            Analyze the following video hook:
            "$hookText"
            For the topic: "$topic" on $platform.

            Evaluate it out of 100 on these 5 dimensions:
            1. Hook Strength
            2. Curiosity Score
            3. Scroll-Stopping Potential
            4. Emotional Impact
            5. Clarity

            Provide the response as a strict, valid JSON object matching this schema. Do not include any extra text, markdown wrappers, or explanation outside the JSON:
            {
              "overallScore": [number 1-100],
              "hookStrength": [number 1-100],
              "curiosityScore": [number 1-100],
              "scrollStoppingPotential": [number 1-100],
              "emotionalImpact": [number 1-100],
              "clarity": [number 1-100],
              "explanation": "[detailed analysis paragraph explaining why the score was given]",
              "weakAreas": ["weak area 1", "weak area 2"],
              "suggestions": ["improvement suggestion 1", "improvement suggestion 2"]
            }
        """.trimIndent()

        val request = GroqRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.5f
        )

        return try {
            val response = apiService.generateChatCompletion("Bearer $apiKey", request)
            val responseText = response.choices?.firstOrNull()?.message?.content ?: ""
            parseHookAnalysis(responseText, hookText, topic, platform)
        } catch (e: Exception) {
            getFallbackHookAnalysis(hookText, topic, platform)
        }
    }

    private fun parseHookAnalysis(
        text: String,
        hookText: String,
        topic: String,
        platform: String
    ): HookAnalysis {
        // Try structured JSON parsing first
        try {
            val startIdx = text.indexOf('{')
            val endIdx = text.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
                val jsonString = text.substring(startIdx, endIdx + 1)
                val adapter = moshi.adapter(HookAnalysis::class.java)
                val parsed = adapter.fromJson(jsonString)
                if (parsed != null) {
                    return parsed
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GroqApiClient", "HookAnalysis JSON parsing failed: ${e.message}. Falling back to string parsing.")
        }

        var overallScore = 80
        var hookStrength = 80
        var curiosityScore = 80
        var scrollStopping = 80
        var emotionalImpact = 80
        var clarity = 80
        var explanation = ""
        val weakAreas = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        var currentSection = ""

        text.split("\n").forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            when {
                line.startsWith("OVERALL_SCORE:", ignoreCase = true) -> {
                    overallScore = line.substringAfter(":").trim().toIntOrNull() ?: overallScore
                }
                line.startsWith("HOOK_STRENGTH:", ignoreCase = true) -> {
                    hookStrength = line.substringAfter(":").trim().toIntOrNull() ?: hookStrength
                }
                line.startsWith("CURIOSITY_SCORE:", ignoreCase = true) -> {
                    curiosityScore = line.substringAfter(":").trim().toIntOrNull() ?: curiosityScore
                }
                line.startsWith("SCROLL_STOPPING:", ignoreCase = true) -> {
                    scrollStopping = line.substringAfter(":").trim().toIntOrNull() ?: scrollStopping
                }
                line.startsWith("EMOTIONAL_IMPACT:", ignoreCase = true) -> {
                    emotionalImpact = line.substringAfter(":").trim().toIntOrNull() ?: emotionalImpact
                }
                line.startsWith("CLARITY:", ignoreCase = true) -> {
                    clarity = line.substringAfter(":").trim().toIntOrNull() ?: clarity
                }
                line.startsWith("EXPLANATION:", ignoreCase = true) -> {
                    currentSection = "EXPLANATION"
                    explanation = line.substringAfter(":").trim()
                }
                line.startsWith("WEAK_AREAS:", ignoreCase = true) -> {
                    currentSection = "WEAK_AREAS"
                }
                line.startsWith("SUGGESTIONS:", ignoreCase = true) -> {
                    currentSection = "SUGGESTIONS"
                }
                line.startsWith("-") || line.startsWith("*") -> {
                    val content = line.substring(1).trim()
                    if (content.isNotEmpty()) {
                        if (currentSection == "WEAK_AREAS") {
                            weakAreas.add(content)
                        } else if (currentSection == "SUGGESTIONS") {
                            suggestions.add(content)
                        }
                    }
                }
                else -> {
                    if (currentSection == "EXPLANATION") {
                        explanation += " " + line
                    }
                }
            }
        }

        if (explanation.isEmpty()) {
            explanation = "Your hook has strong potential. To maximize scroll retention, focus on narrowing the curiosity gap in the first 3 seconds."
        }
        if (weakAreas.isEmpty()) {
            weakAreas.add("Phrasing is slightly standard and lacks an immediate high-stakes claim.")
            weakAreas.add("Audience core pain point is not agitated fast enough.")
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Add a controversial claim or a strong curiosity gap in the first 3 seconds.")
            suggestions.add("Shorten the hook to under 4 seconds to maintain immediate momentum.")
        }

        return HookAnalysis(
            overallScore = overallScore,
            hookStrength = hookStrength,
            curiosityScore = curiosityScore,
            scrollStoppingPotential = scrollStopping,
            emotionalImpact = emotionalImpact,
            clarity = clarity,
            explanation = explanation,
            weakAreas = weakAreas,
            suggestions = suggestions
        )
    }

    fun getFallbackHookAnalysis(
        hookText: String,
        topic: String,
        platform: String
    ): HookAnalysis {
        val hash = Math.abs(hookText.hashCode())
        val score = 75 + (hash % 18)
        return HookAnalysis(
            overallScore = score,
            hookStrength = 70 + (hash % 21),
            curiosityScore = 72 + (hash % 23),
            scrollStoppingPotential = 71 + (hash % 24),
            emotionalImpact = 68 + (hash % 27),
            clarity = 80 + (hash % 15),
            explanation = "Your hook makes a clear attempt to connect with the audience on '$topic'. However, short-form content on $platform requires a higher degree of curiosity and pacing to fully retain viewers past the critical 3-second drop-off mark.",
            weakAreas = listOf(
                "The opening phrasing could benefit from a stronger curiosity loop.",
                "Pacing is slightly standard and lacks an immediate high-stakes claim.",
                "The target audience's core frustration isn't agitated fast enough."
            ),
            suggestions = listOf(
                "Begin with a pattern interrupt: e.g. 'This 1 simple trick...' or 'Stop doing X wrong.'",
                "Inject higher emotional intensity or contrast to stop the infinite scroll.",
                "Shorten the hook to under 4 seconds to leave maximum breathing room for the body."
            )
        )
    }

    suspend fun generateFiveHookVariants(
        topic: String,
        platform: String,
        tone: String,
        scriptBody: String
    ): List<HookVariant> {
        val apiKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            return getFallbackHookVariants(topic, platform, tone)
        }

        val prompt = """
            You are a master of short-form storytelling and conversion copywriting.
            Based on the topic "$topic" on $platform with tone "$tone", and this existing script:
            "$scriptBody"

            Generate exactly 5 alternative opening hooks, each corresponding to one of these frameworks:
            1. Curiosity
            2. Contrarian
            3. Story
            4. Fear of Missing Out
            5. Authority

            Provide the response as a strict, valid JSON object matching this schema. Do not include any extra text, markdown wrappers, or explanation outside the JSON:
            {
              "variants": [
                {
                  "framework": "Curiosity",
                  "hookText": "[hook text]",
                  "shortExplanation": "[brief explanation of why this works]",
                  "predictedScore": 94
                },
                ...
              ]
            }
        """.trimIndent()

        val request = GroqRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.8f
        )

        return try {
            val response = apiService.generateChatCompletion("Bearer $apiKey", request)
            val responseText = response.choices?.firstOrNull()?.message?.content ?: ""
            parseFiveHookVariants(responseText, topic, platform, tone)
        } catch (e: Exception) {
            getFallbackHookVariants(topic, platform, tone)
        }
    }

    private fun parseFiveHookVariants(
        text: String,
        topic: String,
        platform: String,
        tone: String
    ): List<HookVariant> {
        // Try structured JSON parsing first
        try {
            val startIdx = text.indexOf('{')
            val endIdx = text.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
                val jsonString = text.substring(startIdx, endIdx + 1)
                val adapter = moshi.adapter(HookVariantsHolder::class.java)
                val holder = adapter.fromJson(jsonString)
                if (holder != null && holder.variants.isNotEmpty()) {
                    return holder.variants.take(5)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GroqApiClient", "HookVariants JSON parsing failed: ${e.message}. Falling back to block parsing.")
        }

        val variants = mutableListOf<HookVariant>()
        val blocks = text.split(Regex("FRAMEWORK:", RegexOption.IGNORE_CASE))
        
        for (block in blocks) {
            val lines = block.trim().split("\n")
            if (lines.isEmpty() || lines[0].isBlank()) continue

            val framework = lines[0].trim()
            var hook = ""
            var explanation = ""
            var predictedScore = 88

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("HOOK:", ignoreCase = true)) {
                    hook = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                } else if (trimmed.startsWith("EXPLANATION:", ignoreCase = true)) {
                    explanation = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                } else if (trimmed.startsWith("PREDICTED_SCORE:", ignoreCase = true)) {
                    predictedScore = trimmed.substringAfter(":").trim().toIntOrNull() ?: predictedScore
                }
            }

            if (hook.isNotEmpty() && framework.isNotEmpty()) {
                variants.add(
                    HookVariant(
                        framework = framework,
                        hookText = hook,
                        shortExplanation = if (explanation.isEmpty()) "Leverages the $framework framework to trigger instant engagement." else explanation,
                        predictedScore = predictedScore
                    )
                )
            }
        }

        if (variants.size < 5) {
            val existingFrameworks = variants.map { it.framework.lowercase() }
            val fallbacks = getFallbackHookVariants(topic, platform, tone)
            for (f in fallbacks) {
                if (!existingFrameworks.contains(f.framework.lowercase()) && variants.size < 5) {
                    variants.add(f)
                }
            }
        }

        return variants.take(5)
    }

    fun getFallbackHookVariants(
        topic: String,
        platform: String,
        tone: String
    ): List<HookVariant> {
        return listOf(
            HookVariant(
                framework = "Curiosity",
                hookText = "This is why 99% of creators fail at $topic... and how to be the 1% who win.",
                shortExplanation = "Triggers the curiosity gap by implying a hidden secret that viewers desperately want to know.",
                predictedScore = 94
            ),
            HookVariant(
                framework = "Contrarian",
                hookText = "Everything you've been told about $topic is a complete lie. Here's why.",
                shortExplanation = "Pattern-interrupts the audience's feed by directly challenging standard, accepted advice.",
                predictedScore = 91
            ),
            HookVariant(
                framework = "Story",
                hookText = "I lost everything trying to figure out $topic. Until this happened...",
                shortExplanation = "Builds instant empathy and narrative tension through a personal struggle-to-success story hook.",
                predictedScore = 89
            ),
            HookVariant(
                framework = "Fear of Missing Out",
                hookText = "If you don't know this $topic strategy by tomorrow, you will get left behind.",
                shortExplanation = "Agitates urgency and FOMO by highlighting a fast-moving trend or standard viewers must adopt.",
                predictedScore = 93
            ),
            HookVariant(
                framework = "Authority",
                hookText = "After analyzing 1,000 viral videos about $topic, here is the ultimate cheat code.",
                shortExplanation = "Establishes immediate trust and authority by citing high-scale research or expert analysis.",
                predictedScore = 95
            )
        )
    }

    suspend fun generateViralSEOPack(
        topic: String,
        platform: String,
        scriptBody: String
    ): ViralSEODistributionPack {
        val apiKey = try { BuildConfig.GROQ_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            return getFallbackSEOPack(topic, platform)
        }

        val prompt = """
            You are a viral social media SEO strategist and master of digital distribution.
            Based on the topic "$topic", and this short-form script body:
            "$scriptBody"

            Generate a viral SEO pack for 4 platforms: Instagram Reels, YouTube Shorts, TikTok, LinkedIn.

            Provide the response as a strict, valid JSON object matching this schema. Do not include any extra text, markdown wrappers, or explanation outside the JSON:
            {
              "platforms": [
                {
                  "platform": "Instagram Reels",
                  "highCtrTitle": "[High-CTR Title under 50 chars]",
                  "shortCaption": "[Hooky caption under 150 chars]",
                  "longCaption": "[In-depth caption with details]",
                  "hashtags": ["tag1", "tag2", "tag3"],
                  "searchKeywords": ["keyword1", "keyword2"],
                  "ctaSuggestions": ["Call to action suggestion"]
                },
                ...
              ]
            }
        """.trimIndent()

        val request = GroqRequest(
            model = DEFAULT_MODEL,
            messages = listOf(
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.7f
        )

        return try {
            val response = apiService.generateChatCompletion("Bearer $apiKey", request)
            val responseText = response.choices?.firstOrNull()?.message?.content ?: ""
            parseViralSEOPack(responseText, topic, platform)
        } catch (e: Exception) {
            getFallbackSEOPack(topic, platform)
        }
    }

    private fun parseViralSEOPack(
        text: String,
        topic: String,
        platform: String
    ): ViralSEODistributionPack {
        // Try structured JSON parsing first
        try {
            val startIdx = text.indexOf('{')
            val endIdx = text.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
                val jsonString = text.substring(startIdx, endIdx + 1)
                val adapter = moshi.adapter(ViralSEODistributionPack::class.java)
                val parsed = adapter.fromJson(jsonString)
                if (parsed != null && parsed.platforms.isNotEmpty()) {
                    return parsed
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GroqApiClient", "ViralSEOPack JSON parsing failed: ${e.message}. Falling back to block parsing.")
        }

        val platforms = mutableListOf<SEOPlatformPack>()
        val blocks = text.split("===")
        
        for (block in blocks) {
            val trimmedBlock = block.trim()
            if (trimmedBlock.isEmpty()) continue

            val lines = trimmedBlock.split("\n")
            val firstLine = lines[0].trim().replace("=", "").trim()
            if (firstLine.isEmpty()) continue

            var title = ""
            var shortCaption = ""
            var longCaption = ""
            var hashtags = emptyList<String>()
            var keywords = emptyList<String>()
            var cta = ""

            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("TITLE:", ignoreCase = true) -> {
                        title = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                    }
                    trimmed.startsWith("SHORT_CAPTION:", ignoreCase = true) -> {
                        shortCaption = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                    }
                    trimmed.startsWith("LONG_CAPTION:", ignoreCase = true) -> {
                        longCaption = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                    }
                    trimmed.startsWith("HASHTAGS:", ignoreCase = true) -> {
                        hashtags = trimmed.substringAfter(":").trim().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    trimmed.startsWith("KEYWORDS:", ignoreCase = true) -> {
                        keywords = trimmed.substringAfter(":").trim().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    trimmed.startsWith("CTA:", ignoreCase = true) -> {
                        cta = trimmed.substringAfter(":").trim().removeSurrounding("\"")
                    }
                }
            }

            if (title.isNotEmpty()) {
                platforms.add(
                    SEOPlatformPack(
                        platform = firstLine,
                        highCtrTitle = title,
                        shortCaption = shortCaption,
                        longCaption = longCaption,
                        hashtags = hashtags,
                        searchKeywords = keywords,
                        ctaSuggestions = listOf(cta)
                    )
                )
            }
        }

        val platformNames = listOf("Instagram Reels", "YouTube Shorts", "TikTok", "LinkedIn")
        val parsedNames = platforms.map { it.platform.lowercase() }
        
        for (pName in platformNames) {
            if (!parsedNames.any { it.contains(pName.substringBefore(" ").lowercase()) }) {
                platforms.add(getFallbackPlatformPack(topic, pName))
            }
        }

        return ViralSEODistributionPack(platforms = platforms)
    }

    fun getFallbackSEOPack(topic: String, platform: String): ViralSEODistributionPack {
        return ViralSEODistributionPack(
            platforms = listOf(
                getFallbackPlatformPack(topic, "Instagram Reels"),
                getFallbackPlatformPack(topic, "YouTube Shorts"),
                getFallbackPlatformPack(topic, "TikTok"),
                getFallbackPlatformPack(topic, "LinkedIn")
            )
        )
    }

    private fun getFallbackPlatformPack(topic: String, targetPlatform: String): SEOPlatformPack {
        val topicClean = topic.replace(" ", "")
        return when (targetPlatform) {
            "Instagram Reels" -> SEOPlatformPack(
                platform = "Instagram Reels",
                highCtrTitle = "The ultimate $topic cheat code!",
                shortCaption = "Stop doing this wrong! Here is the exact framework to level up your $topic. 🔥",
                longCaption = "If you're still struggling with $topic, you are likely missing these critical steps. Most creators focus on the wrong metrics. Save this Reel to reference next time you build!",
                hashtags = listOf("#$topicClean", "#instagramreels", "#creatorworkflow", "#growthhacks", "#viralreels"),
                searchKeywords = listOf(topic, "instagram reels tips", "content creator", "strategy"),
                ctaSuggestions = listOf("Save this Reel for later!", "Comment 'INFO' to get our free growth roadmap!")
            )
            "YouTube Shorts" -> SEOPlatformPack(
                platform = "YouTube Shorts",
                highCtrTitle = "This 1 change will double your $topic!",
                shortCaption = "The shocking secret to mastering $topic in under 60 seconds. ⚡",
                longCaption = "Want to instantly improve your results with $topic? Here is the exact checklist used by the top 1% of creators to scale fast.",
                hashtags = listOf("#$topicClean", "#shorts", "#youtubeshorts", "#creator", "#viralhack"),
                searchKeywords = listOf(topic, "youtube shorts growth", "how to scale"),
                ctaSuggestions = listOf("Subscribe for daily shorts!", "Check the pinned comment for the template!")
            )
            "TikTok" -> SEOPlatformPack(
                platform = "TikTok",
                highCtrTitle = "TikTok hack they don't want you to know!",
                shortCaption = "You are doing $topic wrong. Do this instead! 😱",
                longCaption = "The secret algorithm loophole for $topic. Don't let your competition find this out before you do.",
                hashtags = listOf("#$topicClean", "#tiktokcreators", "#foryoupage", "#fyp", "#trendingnow"),
                searchKeywords = listOf(topic, "tiktok trends", "viral strategies"),
                ctaSuggestions = listOf("Hit the '+' to join the squad!", "Share this with a friend who needs it!")
            )
            else -> SEOPlatformPack(
                platform = "LinkedIn",
                highCtrTitle = "Why standard $topic methods fail in 2026.",
                shortCaption = "The business of $topic: Why simple workflows beat complex strategies. 💼",
                longCaption = "Many professionals complicate $topic. After examining high-performing teams, we consolidated the three-step framework that actually delivers ROI. Read the full guide below.",
                hashtags = listOf("#$topicClean", "#linkedinstrategy", "#businessgrowth", "#productivity", "#marketing"),
                searchKeywords = listOf(topic, "b2b growth", "professional tips", "execution"),
                ctaSuggestions = listOf("Connect or Follow for weekly insights!", "What are your thoughts? Let's discuss in comments.")
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class HookAnalysis(
    val overallScore: Int = 85,
    val hookStrength: Int = 80,
    val curiosityScore: Int = 85,
    val scrollStoppingPotential: Int = 88,
    val emotionalImpact: Int = 82,
    val clarity: Int = 90,
    val explanation: String = "",
    val weakAreas: List<String> = emptyList(),
    val suggestions: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class HookVariant(
    val framework: String,
    val hookText: String,
    val shortExplanation: String,
    val predictedScore: Int
)

@JsonClass(generateAdapter = true)
data class HookVariantsHolder(
    val variants: List<HookVariant>
)

@JsonClass(generateAdapter = true)
data class SEOPlatformPack(
    val platform: String,
    val highCtrTitle: String,
    val shortCaption: String,
    val longCaption: String,
    val hashtags: List<String>,
    val searchKeywords: List<String>,
    val ctaSuggestions: List<String>
)

@JsonClass(generateAdapter = true)
data class ViralSEODistributionPack(
    val platforms: List<SEOPlatformPack> = emptyList()
)

