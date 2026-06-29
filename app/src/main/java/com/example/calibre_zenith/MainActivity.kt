package com.example.calibre_zenith

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.calibre_zenith.ui.theme.CalibreZenithTheme
import com.example.calibre_zenith.ui.theme.components.BossDamageOverlay
import com.example.calibre_zenith.ui.theme.screens.*
import com.example.calibre_zenith.ui.viewmodel.CombatViewModel
import com.example.calibre_zenith.ui.viewmodel.PauseViewModel

class MainActivity : ComponentActivity() {

    private val pauseViewModel: PauseViewModel by viewModels()
    private val combatViewModel: CombatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIncomingDeepLink(intent)

        setContent {
            CalibreZenithTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    val currentScreen = pauseViewModel.currentScreen

                    when (currentScreen) {
                        "Dashboard" -> DashboardScreen(viewModel = pauseViewModel)
                        "Planner" -> PlannerScreen(viewModel = pauseViewModel, combatViewModel = combatViewModel)
                        "Pause" -> PauseScreen(viewModel = pauseViewModel)
                        "Timer" -> CognitiveTimerScreen(viewModel = pauseViewModel)
                        "Generating" -> GeneratingScreen()
                        "Roadmap" -> RoadmapScreen(viewModel = pauseViewModel, combatViewModel = combatViewModel)

                        "BossWorkshop" -> BossCreationScreen(
                            pauseViewModel = pauseViewModel,
                            combatViewModel = combatViewModel
                        )

                        "Combat" -> CombatScreen(
                            pauseViewModel = pauseViewModel,
                            combatViewModel = combatViewModel
                        )

                        else -> DashboardScreen(viewModel = pauseViewModel)
                    }

                    BossDamageOverlay(viewModel = combatViewModel)
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

            pauseViewModel.loadCognitiveSession(
                title = title,
                launchTrigger = microStep,
                resistanceProfile = friction,
                isFromScheduledTask = true
            )
            pauseViewModel.navigateToTimerScreen()
        }
    }
}