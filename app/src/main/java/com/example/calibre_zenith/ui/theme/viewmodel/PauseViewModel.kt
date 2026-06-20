package com.example.calibre_zenith.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calibre_zenith.data.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PauseViewModel : ViewModel() {

    private val geminiRepository = GeminiRepository()

    // --- NAVIGATION & UI STATE ---
    var currentScreen by mutableStateOf("Dashboard")
    var sessionTitle by mutableStateOf("")
    var sessionLaunchTrigger by mutableStateOf("")
    var sessionResistanceProfile by mutableStateOf("")
    var sessionMeaning by mutableStateOf("")
    var isFromScheduledTask by mutableStateOf(false)
    var secondsLeft by mutableStateOf(0)

    // Baseline fallback pool for affirmations
    var affirmations by mutableStateOf<List<String>>(
        listOf(
            "ACTION PRECEDES MOMENTUM. YOU DO NOT NEED CLARITY TO BEGIN.",
            "YOUR UNIVERSE HAS SHRUNK TO EXACTLY THIS STEP. THE REST IS OFFLINE.",
            "GIVE YOURSELF ABSOLUTE CLEARANCE TO PRODUCE A CHAOTIC FIRST ATTEMPT."
        )
    )

    var isGeminiOptimizing by mutableStateOf(false)

    // --- GEMINI DATA FLOWS (Required by PreFlightScreen) ---
    private val _taskName = MutableStateFlow("")
    private val _microStep = MutableStateFlow("")
    private val _selectedMonster = MutableStateFlow("Perfectionism Paralysis")
    private val _selectedHook = MutableStateFlow("Terminal Interface")
    private val _sessionDurationSeconds = MutableStateFlow(5 * 60)
    private val _directivesState = MutableStateFlow<List<String>>(emptyList())

    val taskName: StateFlow<String> = _taskName.asStateFlow()
    val microStep: StateFlow<String> = _microStep.asStateFlow()
    val selectedMonster: StateFlow<String> = _selectedMonster.asStateFlow()
    val selectedHook: StateFlow<String> = _selectedHook.asStateFlow()
    val sessionDurationSeconds: StateFlow<Int> = _sessionDurationSeconds.asStateFlow()
    val directivesState: StateFlow<List<String>> = _directivesState.asStateFlow()

    // --- NAVIGATION FUNCTIONS (Required by Dashboard/Screens) ---
    fun navigateToDashboard() { currentScreen = "Dashboard" }
    fun navigateToPlanner() { currentScreen = "Planner" }
    fun navigateToTimerScreen() { currentScreen = "Timer" }
    fun navigateToPreFlight() { currentScreen = "PreFlight" }
    fun navigateToPauseScreen() { currentScreen = "Pause" }

    fun clearSessionAndGoBack() {
        sessionTitle = ""
        sessionLaunchTrigger = ""
        sessionResistanceProfile = ""
        sessionMeaning = ""
        secondsLeft = 0
        currentScreen = "Dashboard"
    }

    // --- SESSION LOADERS (Required by MainActivity) ---
    fun loadCognitiveSession(
        title: String,
        launchTrigger: String,
        resistanceProfile: String,
        isFromScheduledTask: Boolean
    ) {
        sessionTitle = title
        sessionLaunchTrigger = launchTrigger
        sessionResistanceProfile = resistanceProfile
        this.isFromScheduledTask = isFromScheduledTask

        _taskName.value = title
        _microStep.value = launchTrigger
        _selectedMonster.value = resistanceProfile
    }

    fun startManualSession() {
        sessionTitle = ""
        sessionLaunchTrigger = ""
        sessionResistanceProfile = ""
        sessionMeaning = ""
        isFromScheduledTask = false
        currentScreen = "Timer"
    }

    // --- BRIDGING ENGINE ---
    fun launchTileEngine(
        title: String,
        trigger: String,
        resistance: String,
        meaning: String,
        durationMinutes: Int
    ) {
        sessionTitle = title
        sessionLaunchTrigger = trigger
        sessionResistanceProfile = resistance
        sessionMeaning = meaning
        secondsLeft = durationMinutes * 60

        _taskName.value = title
        _microStep.value = trigger
        _selectedMonster.value = resistance
        _selectedHook.value = meaning
        _sessionDurationSeconds.value = durationMinutes * 60

        viewModelScope.launch {
            isGeminiOptimizing = true
            try {
                val processedResult = geminiRepository.fetchReframedDirectives(
                    taskName = title,
                    microStep = trigger,
                    monster = resistance
                )
                sessionLaunchTrigger = processedResult.deconstructedStep
                _microStep.value = processedResult.deconstructedStep
                affirmations = processedResult.affirmations
                _directivesState.value = processedResult.affirmations
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isGeminiOptimizing = false
            }
        }
        currentScreen = "Timer"
    }

    fun tickDownOneSecond() {
        if (secondsLeft > 0) secondsLeft--
    }

    // --- STATE DISPATCHERS (Required by PreFlightScreen) ---
    fun updateTaskName(value: String) { _taskName.value = value }
    fun updateMicroStep(value: String) { _microStep.value = value }
    fun updateSelectedMonster(value: String) { _selectedMonster.value = value }
    fun updateSelectedHook(value: String) { _selectedHook.value = value }
    fun updateSessionDurationSeconds(seconds: Int) { _sessionDurationSeconds.value = seconds }

    // --- WORKFLOW ENGINE (Required by PreFlightScreen) ---
    suspend fun launchCognitiveSessionWorkflow(context: Context) {
        isGeminiOptimizing = true
        try {
            val processedResult = geminiRepository.fetchReframedDirectives(
                taskName = _taskName.value,
                microStep = _microStep.value,
                monster = _selectedMonster.value
            )
            sessionLaunchTrigger = processedResult.deconstructedStep
            _microStep.value = processedResult.deconstructedStep
            affirmations = processedResult.affirmations
            _directivesState.value = processedResult.affirmations
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            isGeminiOptimizing = false
        }
    }
}