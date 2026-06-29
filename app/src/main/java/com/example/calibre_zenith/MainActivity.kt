package com.example.calibre_zenith

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.calibre_zenith.ui.screen.PreFlightScreen
import com.example.calibre_zenith.ui.theme.CalibreZenithTheme
import com.example.calibre_zenith.ui.theme.screens.CombatScreen
import com.example.calibre_zenith.ui.theme.screens.CognitiveTimerScreen
import com.example.calibre_zenith.ui.theme.screens.DashboardScreen
import com.example.calibre_zenith.ui.theme.screens.GeneratingScreen
import com.example.calibre_zenith.ui.theme.screens.PauseScreen
import com.example.calibre_zenith.ui.theme.screens.PlannerScreen
import com.example.calibre_zenith.ui.theme.screens.RoadmapScreen
import com.example.calibre_zenith.ui.viewmodel.CombatViewModel
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel

class MainActivity : ComponentActivity() {

    // ← single instance of each, no duplicates
    private val pauseViewModel: PauseViewModel by viewModels()
    private val combatViewModel: CombatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingDeepLink(intent)

        setContent {
            CalibreZenithTheme {
                val currentScreen = pauseViewModel.currentScreen

                when (currentScreen) {
                    "Dashboard"  -> DashboardScreen(viewModel = pauseViewModel)
                    "Planner"    -> PlannerScreen(viewModel = pauseViewModel)
                    "PreFlight"  -> PreFlightScreen(viewModel = pauseViewModel)
                    "Pause"      -> PauseScreen(viewModel = pauseViewModel)
                    "Timer"      -> CognitiveTimerScreen(viewModel = pauseViewModel)
                    "Generating" -> GeneratingScreen()
                    "Roadmap"    -> RoadmapScreen(viewModel = pauseViewModel)
                    "Combat"     -> CombatScreen(
                        pauseViewModel  = pauseViewModel,
                        combatViewModel = combatViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingDeepLink(intent)
    }

    private fun handleIncomingDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "calibre" && data.host == "timer") {
            val title     = data.getQueryParameter("title") ?: ""
            val microStep = data.getQueryParameter("microStep") ?: ""
            val friction  = data.getQueryParameter("friction") ?: ""

            pauseViewModel.loadCognitiveSession(
                title               = title,
                launchTrigger       = microStep,
                resistanceProfile   = friction,
                isFromScheduledTask = true
            )
            pauseViewModel.navigateToTimerScreen()
        }
    }
}