package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ScriptEntity
import com.example.data.repository.ScriptRepository
import com.example.data.network.GroqApiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface GenerationState {
    object Idle : GenerationState
    object Loading : GenerationState
    data class Success(val scriptText: String) : GenerationState
    data class Error(val message: String) : GenerationState
}

sealed class Screen {
    object Welcome : Screen()
    object Dashboard : Screen()
    object Creator : Screen()
    data class Preview(val scriptId: Int?, val isNew: Boolean = false) : Screen()
    data class Teleprompter(val scriptId: Int) : Screen()
    object Paywall : Screen()
}

class ScriptViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ScriptRepository(database.scriptDao())

    // --- Navigation State ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen navigation stack for back navigation
    private val screenStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        screenStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    // --- Saved Scripts Flow ---
    val savedScripts: StateFlow<List<ScriptEntity>> = repository.allScripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val prefs = application.getSharedPreferences("focalscribe_prefs", android.content.Context.MODE_PRIVATE)

    // --- Subscription / Pro Quota State ---
    private val _isProUser = MutableStateFlow(prefs.getBoolean("is_pro_user", false))
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _scriptsGeneratedCount = MutableStateFlow(0)
    val scriptsGeneratedCount: StateFlow<Int> = _scriptsGeneratedCount.asStateFlow()

    val maxFreeScripts = 5

    init {
        // Daily limit check and resets
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val lastGenDate = prefs.getString("last_generation_date", "")
        if (lastGenDate != todayStr) {
            prefs.edit()
                .putString("last_generation_date", todayStr)
                .putInt("scripts_generated_count", 0)
                .apply()
            _scriptsGeneratedCount.value = 0
        } else {
            _scriptsGeneratedCount.value = prefs.getInt("scripts_generated_count", 0)
        }
        
        // Log App Open
        com.example.analytics.FocalScribeAnalytics.logAppOpen(application)
    }

    fun unlockPro(planName: String = "Founding Creator Plan", price: String = "₹99") {
        _isProUser.value = true
        prefs.edit().putBoolean("is_pro_user", true).apply()
        com.example.analytics.FocalScribeAnalytics.logSubscriptionPurchased(
            getApplication(),
            planName,
            price
        )
        // Log trial conversion and launch analytics events
        com.example.analytics.FocalScribeAnalytics.logTrialUserConverted(getApplication())
        if (planName.contains("Founding", ignoreCase = true)) {
            com.example.analytics.FocalScribeAnalytics.logFoundingPlanPurchased(getApplication())
        } else {
            com.example.analytics.FocalScribeAnalytics.logProPlanPurchased(getApplication())
        }
    }

    // --- Script Creator Form States ---
    val topic = MutableStateFlow("")
    val selectedPlatform = MutableStateFlow("Instagram Reels")
    val selectedTone = MutableStateFlow("Hype")
    val selectedHookStyle = MutableStateFlow("Alex Hormozi Style")
    val selectedDuration = MutableStateFlow(60)
    val customPrompt = MutableStateFlow("")

    // --- Generation State ---
    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    // Active script under preview or editing
    private val _activeScript = MutableStateFlow<ScriptEntity?>(null)
    val activeScript: StateFlow<ScriptEntity?> = _activeScript.asStateFlow()

    // --- Premium MVP v2 States ---
    private val _abHooks = MutableStateFlow<List<String>>(emptyList())
    val abHooks: StateFlow<List<String>> = _abHooks.asStateFlow()

    private val _isLoadingAbHooks = MutableStateFlow(false)
    val isLoadingAbHooks: StateFlow<Boolean> = _isLoadingAbHooks.asStateFlow()

    private val _generatedMetadata = MutableStateFlow<String?>(null)
    val generatedMetadata: StateFlow<String?> = _generatedMetadata.asStateFlow()

    private val _isLoadingMetadata = MutableStateFlow(false)
    val isLoadingMetadata: StateFlow<Boolean> = _isLoadingMetadata.asStateFlow()

    private val _isShiftingTone = MutableStateFlow(false)
    val isShiftingTone: StateFlow<Boolean> = _isShiftingTone.asStateFlow()

    // --- Hook Score Engine States ---
    private val _hookAnalysis = MutableStateFlow<com.example.data.network.HookAnalysis?>(null)
    val hookAnalysis: StateFlow<com.example.data.network.HookAnalysis?> = _hookAnalysis.asStateFlow()

    private val _isAnalyzingHook = MutableStateFlow(false)
    val isAnalyzingHook: StateFlow<Boolean> = _isAnalyzingHook.asStateFlow()

    // --- Multi-Hook Variant Generator States ---
    private val _fiveHookVariants = MutableStateFlow<List<com.example.data.network.HookVariant>>(emptyList())
    val fiveHookVariants: StateFlow<List<com.example.data.network.HookVariant>> = _fiveHookVariants.asStateFlow()

    private val _isLoadingFiveHookVariants = MutableStateFlow(false)
    val isLoadingFiveHookVariants: StateFlow<Boolean> = _isLoadingFiveHookVariants.asStateFlow()

    // --- Viral SEO Distribution Pack States ---
    private val _viralSEOPack = MutableStateFlow<com.example.data.network.ViralSEODistributionPack?>(null)
    val viralSEOPack: StateFlow<com.example.data.network.ViralSEODistributionPack?> = _viralSEOPack.asStateFlow()

    private val _isLoadingViralSEOPack = MutableStateFlow(false)
    val isLoadingViralSEOPack: StateFlow<Boolean> = _isLoadingViralSEOPack.asStateFlow()

    // --- Teleprompter Configuration ---
    val isPlaying = MutableStateFlow(false)
    val scrollSpeed = MutableStateFlow(25f) // characters per second (approx)
    val fontSize = MutableStateFlow(28f) // sp
    val isMirrored = MutableStateFlow(false)

    fun resetPremiumStates() {
        _abHooks.value = emptyList()
        _generatedMetadata.value = null
        _hookAnalysis.value = null
        _fiveHookVariants.value = emptyList()
        _viralSEOPack.value = null
    }

    // --- Script Actions ---
    fun selectScriptForPreview(scriptId: Int) {
        resetPremiumStates()
        viewModelScope.launch {
            val script = repository.getScriptById(scriptId)
            _activeScript.value = script
            navigateTo(Screen.Preview(scriptId))
        }
    }

    fun selectScriptForTeleprompter(scriptId: Int) {
        viewModelScope.launch {
            val script = repository.getScriptById(scriptId)
            _activeScript.value = script
            // Reset teleprompter play state
            isPlaying.value = false
            
            // Log teleprompter started event
            val estDuration = getEstimatedDurationSeconds(script?.fullText ?: "")
            com.example.analytics.FocalScribeAnalytics.logTeleprompterStarted(
                getApplication(),
                estDuration,
                scrollSpeed.value
            )
            
            navigateTo(Screen.Teleprompter(scriptId))
        }
    }

    fun createNewScript() {
        // Reset creator form
        topic.value = ""
        customPrompt.value = ""
        _generationState.value = GenerationState.Idle
        _activeScript.value = null
        resetPremiumStates()
        navigateTo(Screen.Creator)
    }

    fun generateAIScript() {
        val currentTopic = topic.value.trim()
        if (currentTopic.isEmpty()) {
            _generationState.value = GenerationState.Error("Please enter a topic.")
            return
        }

        // Quota check
        if (!_isProUser.value && _scriptsGeneratedCount.value >= maxFreeScripts) {
            com.example.analytics.FocalScribeAnalytics.logFreeLimitReached(getApplication())
            com.example.analytics.FocalScribeAnalytics.logUpgradeViewed(getApplication(), "Script limit reached")
            navigateTo(Screen.Paywall)
            return
        }

        viewModelScope.launch {
            _generationState.value = GenerationState.Loading
            try {
                val scriptText = GroqApiClient.generateScript(
                    topic = currentTopic,
                    platform = selectedPlatform.value,
                    tone = selectedTone.value,
                    hookStyle = selectedHookStyle.value,
                    durationSeconds = selectedDuration.value,
                    customPrompt = customPrompt.value
                )

                _generationState.value = GenerationState.Success(scriptText)
                _scriptsGeneratedCount.value += 1
                prefs.edit().putInt("scripts_generated_count", _scriptsGeneratedCount.value).apply()

                // Log script generated event
                com.example.analytics.FocalScribeAnalytics.logScriptGenerated(
                    getApplication(),
                    selectedPlatform.value,
                    selectedTone.value,
                    selectedDuration.value
                )

                // Create a temporary un-saved active script
                _activeScript.value = ScriptEntity(
                    title = "Script: ${if (currentTopic.length > 25) currentTopic.take(25) + "..." else currentTopic}",
                    topic = currentTopic,
                    platform = selectedPlatform.value,
                    tone = selectedTone.value,
                    hookStyle = selectedHookStyle.value,
                    fullText = scriptText,
                    durationSeconds = selectedDuration.value,
                    isFavorite = false
                )

                navigateTo(Screen.Preview(scriptId = null, isNew = true))
            } catch (e: Exception) {
                _generationState.value = GenerationState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun saveActiveScript() {
        val script = _activeScript.value ?: return
        viewModelScope.launch {
            val id = repository.insertScript(script)
            val savedScript = script.copy(id = id.toInt())
            _activeScript.value = savedScript
        }
    }

    fun updateActiveScriptText(newText: String) {
        val script = _activeScript.value ?: return
        val updated = script.copy(fullText = newText)
        _activeScript.value = updated
        viewModelScope.launch {
            if (updated.id != 0) {
                repository.updateScript(updated)
            }
        }
    }

    fun toggleFavorite(script: ScriptEntity) {
        viewModelScope.launch {
            val updated = script.copy(isFavorite = !script.isFavorite)
            repository.updateScript(updated)
            if (_activeScript.value?.id == script.id) {
                _activeScript.value = updated
            }
        }
    }

    fun deleteScript(script: ScriptEntity) {
        viewModelScope.launch {
            repository.deleteScript(script)
            if (_activeScript.value?.id == script.id) {
                _activeScript.value = null
            }
        }
    }

    fun deleteScriptById(id: Int) {
        viewModelScope.launch {
            repository.deleteScriptById(id)
            if (_activeScript.value?.id == id) {
                _activeScript.value = null
            }
        }
    }

    // --- Premium MVP v2 Methods ---
    fun generateAbHooks() {
        val script = _activeScript.value ?: return
        viewModelScope.launch {
            _isLoadingAbHooks.value = true
            try {
                val hooks = GroqApiClient.generateAlternativeHooks(
                    topic = script.topic,
                    platform = script.platform,
                    tone = script.tone,
                    hookStyle = script.hookStyle,
                    scriptBody = script.fullText
                )
                _abHooks.value = hooks
            } catch (e: Exception) {
                _abHooks.value = listOf(
                    "⚡ Variant 1: Want to master ${script.topic}? This 1 change is all it takes.",
                    "🚨 Variant 2: STOP scrolling if you care about ${script.topic}.",
                    "🧠 Variant 3: The secret nobody tells you about ${script.topic}."
                )
            } finally {
                _isLoadingAbHooks.value = false
            }
        }
    }

    fun swapHook(newHook: String) {
        val script = _activeScript.value ?: return
        val updatedText = replaceHookText(script.fullText, newHook)
        updateActiveScriptText(updatedText)
    }

    private fun replaceHookText(fullText: String, newHook: String): String {
        val lines = fullText.split("\n").toMutableList()
        var startIdx = -1
        var endIdx = -1
        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.contains("[Hook", ignoreCase = true)) {
                startIdx = i + 1
                // find next bracket or next empty line
                for (j in startIdx until lines.size) {
                    val nextLine = lines[j].trim()
                    if (nextLine.startsWith("[") || (nextLine.isEmpty() && j > startIdx)) {
                        endIdx = j
                        break
                    }
                }
                if (endIdx == -1) endIdx = lines.size
                break
            }
        }
        
        return if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
            val result = mutableListOf<String>()
            result.addAll(lines.subList(0, startIdx))
            result.add(newHook)
            result.addAll(lines.subList(endIdx, lines.size))
            result.joinToString("\n")
        } else {
            "[Hook]\n\"$newHook\"\n\n$fullText"
        }
    }

    fun getWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    fun getEstimatedDurationSeconds(text: String): Int {
        val words = getWordCount(text)
        return if (words == 0) 0 else (words / 140f * 60f).toInt()
    }

    fun generateViralMetadata() {
        val script = _activeScript.value ?: return
        viewModelScope.launch {
            _isLoadingMetadata.value = true
            try {
                val result = GroqApiClient.generateViralMetadata(
                    topic = script.topic,
                    platform = script.platform,
                    tone = script.tone,
                    scriptText = script.fullText
                )
                _generatedMetadata.value = result
            } catch (e: Exception) {
                _generatedMetadata.value = """
                    📌 **VIRAL CAPTION**
                    "Stop wasting time doing this wrong! Here is the exact framework to level up your ${script.topic}."
                    
                    🔥 **HIGH-CTR TITLES (Use on Cover)**
                    1. The ${script.platform} hack you're ignoring!
                    2. Do this for 10x better ${script.topic}
                    3. The truth about ${script.topic}
                    
                    🏷️ **BEST SEO HASHTAGS**
                    #${script.topic.replace(" ", "")} #creatorworkflow #growthhacks #viralshorts
                """.trimIndent()
            } finally {
                _isLoadingMetadata.value = false
            }
        }
    }

    fun resetMetadata() {
        _generatedMetadata.value = null
    }

    fun shiftActiveScriptTone(targetTone: String) {
        val script = _activeScript.value ?: return
        viewModelScope.launch {
            _isShiftingTone.value = true
            try {
                val rewrittenBody = GroqApiClient.rewriteScriptTone(
                    scriptText = script.fullText,
                    originalTone = script.tone,
                    targetTone = targetTone
                )
                val updated = script.copy(
                    tone = targetTone,
                    fullText = rewrittenBody
                )
                _activeScript.value = updated
                if (updated.id != 0) {
                    repository.updateScript(updated)
                }
            } catch (e: Exception) {
                // fallback rewrite
                val updated = script.copy(
                    tone = targetTone,
                    fullText = "[Rewritten in $targetTone tone]\n\n" + script.fullText.substringAfter("\n\n")
                )
                _activeScript.value = updated
                if (updated.id != 0) {
                    repository.updateScript(updated)
                }
            } finally {
                _isShiftingTone.value = false
            }
        }
    }

    fun extractHookFromScript(text: String): String {
        val lines = text.split("\n")
        val hookLines = mutableListOf<String>()
        var inHookSection = false
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[Hook", ignoreCase = true)) {
                inHookSection = true
                continue
            }
            if (trimmed.startsWith("[Body", ignoreCase = true) || trimmed.startsWith("[CTA", ignoreCase = true) || (trimmed.startsWith("[") && trimmed.endsWith("]") && !trimmed.contains("Hook", ignoreCase = true))) {
                if (hookLines.isNotEmpty()) break
                inHookSection = false
            }
            if (inHookSection && trimmed.isNotEmpty() && !trimmed.startsWith("[")) {
                hookLines.add(trimmed)
            }
        }
        
        if (hookLines.isNotEmpty()) {
            return hookLines.joinToString(" ").removeSurrounding("\"")
        }
        
        val nonCueLines = lines.map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("[") }
        return nonCueLines.take(2).joinToString(" ").removeSurrounding("\"")
    }

    fun analyzeActiveScriptHook() {
        val script = _activeScript.value ?: return
        val extractedHook = extractHookFromScript(script.fullText)
        
        viewModelScope.launch {
            _isAnalyzingHook.value = true
            try {
                val analysis = GroqApiClient.analyzeHook(
                    hookText = extractedHook,
                    topic = script.topic,
                    platform = script.platform
                )
                _hookAnalysis.value = analysis
                
                // Log hook analyzed event
                com.example.analytics.FocalScribeAnalytics.logHookGenerated(
                    getApplication(),
                    "Analysis",
                    analysis.overallScore
                )
            } catch (e: Exception) {
                _hookAnalysis.value = GroqApiClient.getFallbackHookAnalysis(extractedHook, script.topic, script.platform)
            } finally {
                _isAnalyzingHook.value = false
            }
        }
    }

    fun generateFiveHookVariants() {
        val script = _activeScript.value ?: return
        viewModelScope.launch {
            _isLoadingFiveHookVariants.value = true
            try {
                val variants = GroqApiClient.generateFiveHookVariants(
                    topic = script.topic,
                    platform = script.platform,
                    tone = script.tone,
                    scriptBody = script.fullText
                )
                _fiveHookVariants.value = variants
                
                // Log hook variants event
                com.example.analytics.FocalScribeAnalytics.logHookGenerated(
                    getApplication(),
                    "5 Variants",
                    variants.firstOrNull()?.predictedScore ?: 90
                )
            } catch (e: Exception) {
                _fiveHookVariants.value = GroqApiClient.getFallbackHookVariants(script.topic, script.platform, script.tone)
            } finally {
                _isLoadingFiveHookVariants.value = false
            }
        }
    }

    fun applyHookVariant(newHook: String) {
        val script = _activeScript.value ?: return
        val lines = script.fullText.split("\n").toMutableList()
        var inHook = false
        var hookIndexStart = -1
        var hookIndexEnd = -1

        for (i in lines.indices) {
            val trimmed = lines[i].trim()
            if (trimmed.startsWith("[Hook", ignoreCase = true)) {
                inHook = true
                hookIndexStart = i
                continue
            }
            if (inHook && (trimmed.startsWith("[Body", ignoreCase = true) || trimmed.startsWith("[CTA", ignoreCase = true) || (trimmed.startsWith("[") && trimmed.endsWith("]")))) {
                hookIndexEnd = i
                break
            }
        }

        val finalScriptText = if (hookIndexStart != -1) {
            val endIdx = if (hookIndexEnd != -1) hookIndexEnd else lines.size
            val headerLine = lines[hookIndexStart]
            val before = lines.subList(0, hookIndexStart + 1)
            val after = lines.subList(endIdx, lines.size)
            val newHookLines = listOf("\"$newHook\"")
            (before + newHookLines + after).joinToString("\n")
        } else {
            var replaced = false
            val newLines = lines.map { line ->
                val t = line.trim()
                if (!replaced && t.isNotEmpty() && !t.startsWith("[")) {
                    replaced = true
                    "\"$newHook\""
                } else {
                    line
                }
            }
            newLines.joinToString("\n")
        }

        updateActiveScriptText(finalScriptText)
        analyzeActiveScriptHook()
    }

    fun generateViralSEODistributionPack() {
        val script = _activeScript.value ?: return
        viewModelScope.launch {
            _isLoadingViralSEOPack.value = true
            try {
                val pack = GroqApiClient.generateViralSEOPack(
                    topic = script.topic,
                    platform = script.platform,
                    scriptBody = script.fullText
                )
                _viralSEOPack.value = pack
                
                // Log SEO pack event
                com.example.analytics.FocalScribeAnalytics.logSEOPackGenerated(
                    getApplication(),
                    script.topic
                )
            } catch (e: Exception) {
                _viralSEOPack.value = GroqApiClient.getFallbackSEOPack(script.topic, script.platform)
            } finally {
                _isLoadingViralSEOPack.value = false
            }
        }
    }
}
