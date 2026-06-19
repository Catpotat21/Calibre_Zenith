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
    var isFromScheduledTask by mutableStateOf(false)
    var secondsLeft by mutableStateOf(0)

    // Was `mutableStateOf("")` (a String) but PauseScreen.kt indexes it like a list
    // (affirmations[i], affirmations.size) - fixed to match actual usage.
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
    }

    // TEMPORARY best-guess implementation - paste DashboardScreen.kt's call site and I'll
    // match the exact signature/params it's actually being called with.
    fun startManualSession() {
        sessionTitle = ""
        sessionLaunchTrigger = ""
        sessionResistanceProfile = ""
        isFromScheduledTask = false
        currentScreen = "Timer"
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