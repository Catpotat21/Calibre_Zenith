package com.example.calibre_zenith

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.calibre_zenith.ui.screen.PreFlightScreen
import com.example.calibre_zenith.ui.theme.CalibreZenithTheme
import com.example.calibre_zenith.ui.theme.screens.PlannerScreen
import com.example.calibre_zenith.ui.theme.screens.CognitiveTimerScreen
import com.example.calibre_zenith.ui.theme.screens.DashboardScreen
import com.example.calibre_zenith.ui.theme.screens.PauseScreen
import com.example.calibre_zenith.ui.theme.screens.GeneratingScreen
import com.example.calibre_zenith.ui.theme.screens.RoadmapScreen // Added Roadmap import
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PauseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingDeepLink(intent)

        setContent {
            CalibreZenithTheme {
                // Observable Compose State Routing
                val currentScreen = viewModel.currentScreen

                when (currentScreen) {
                    "Dashboard" -> DashboardScreen(viewModel = viewModel)
                    "Planner" -> PlannerScreen(viewModel = viewModel)
                    "PreFlight" -> PreFlightScreen(viewModel = viewModel)
                    "Pause" -> PauseScreen(viewModel = viewModel)
                    "Timer" -> CognitiveTimerScreen(viewModel = viewModel)
                    "Generating" -> GeneratingScreen()
                    "Roadmap" -> RoadmapScreen() // Added Roadmap route
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
            val title = data.getQueryParameter("title") ?: ""
            val microStep = data.getQueryParameter("microStep") ?: ""
            val friction = data.getQueryParameter("friction") ?: ""

            viewModel.loadCognitiveSession(
                title = title,
                launchTrigger = microStep,
                resistanceProfile = friction,
                isFromScheduledTask = true
            )

            viewModel.navigateToTimerScreen()
        }
    }
}