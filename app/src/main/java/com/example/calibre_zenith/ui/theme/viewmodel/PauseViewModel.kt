package com.example.calibre_zenith.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.calibre_zenith.data.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PauseViewModel : ViewModel() {

    private val geminiRepository = GeminiRepository()

    // --- NAVIGATION & UI STATE (Compose State) ---
    var currentScreen by mutableStateOf("Dashboard")
    var sessionTitle by mutableStateOf("")
    var sessionLaunchTrigger by mutableStateOf("")
    var sessionResistanceProfile by mutableStateOf("")
    var sessionMeaning by mutableStateOf("") // ✨ Added: Clean home for Step 4 value assignment
    var isFromScheduledTask by mutableStateOf(false)
    var secondsLeft by mutableStateOf(0)

    // Match actual list usage in PauseScreen
    var affirmations by mutableStateOf<List<String>>(emptyList())

    // --- GEMINI DATA FLOWS ---
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

    // --- NAVIGATION FUNCTIONS ---
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

    // --- SESSION LOADERS ---
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

        // Sync to Gemini pipeline in case it fires immediately
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

    // --- ✨ THE BRIDGING ENGINE: CONNECTS WIZARD TO TILE GAME ---
    fun launchTileEngine(
        title: String,
        trigger: String,
        resistance: String,
        meaning: String,
        durationMinutes: Int
    ) {
        // 1. Commit metrics to Compose States (Instant UI reading)
        sessionTitle = title
        sessionLaunchTrigger = trigger
        sessionResistanceProfile = resistance
        sessionMeaning = meaning
        secondsLeft = durationMinutes * 60

        // 2. Commit metrics to Gemini StateFlow streams
        _taskName.value = title
        _microStep.value = trigger
        _selectedMonster.value = resistance
        _selectedHook.value = meaning
        _sessionDurationSeconds.value = durationMinutes * 60

        // 3. Shift the structural window over to the Game/Timer arena
        currentScreen = "Timer"
    }

    // --- ✨ TIMER CORE: Helper function for your game clock tick ---
    fun tickDownOneSecond() {
        if (secondsLeft > 0) {
            secondsLeft--
        }
    }

    // --- STATE UPDATE MATRIX DISPATCHERS ---
    fun updateTaskName(value: String) { _taskName.value = value }
    fun updateMicroStep(value: String) { _microStep.value = value }
    fun updateSelectedMonster(value: String) { _selectedMonster.value = value }
    fun updateSelectedHook(value: String) { _selectedHook.value = value }
    fun updateSessionDurationSeconds(seconds: Int) { _sessionDurationSeconds.value = seconds }

    // --- COGNITIVE WORKFLOW ENGINE ---
    suspend fun launchCognitiveSessionWorkflow(context: Context) {
        try {
            val liveDirectives = geminiRepository.fetchReframedDirectives(
                taskName = _taskName.value,
                microStep = _microStep.value,
                monster = _selectedMonster.value
            )
            _directivesState.value = liveDirectives
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}