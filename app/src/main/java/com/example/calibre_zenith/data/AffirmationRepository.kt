package com.example.calibre_zenith.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

data class CatProfile(
    val id: String,
    val breed: String,
    val sleepColor: Color,
    val wakeColor: Color,
    val glowColor: Color
)

object CyberCatMatrix {
    val matrix = listOf(
        CatProfile("calico", "Calico", Color(0xFF2A1E17), Color(0xFFFF9F43), Color(0xFFFFC58D)),
        CatProfile("tuxedo", "Tuxedo", Color(0xFF1A1B26), Color(0xFF54A0FF), Color(0xFF9AD2FF)),
        CatProfile("ginger", "Ginger", Color(0xFF2E1C12), Color(0xFFFF6B6B), Color(0xFFFFA8A8)),
        CatProfile("tabby", "Tabby", Color(0xFF142426), Color(0xFF1DD1A1), Color(0xFF6DF1D2)),
        CatProfile("bengal", "Bengal", Color(0xFF2C2415), Color(0xFFFECA57), Color(0xFFFFE49E)),
        CatProfile("mystic", "Mystic", Color(0xFF25182D), Color(0xFF9B5DE5), Color(0xFFD3B0FF)),
        CatProfile("phantom", "Phantom", Color(0xFF1C1E24), Color(0xFF00FFCC), Color(0xFF80FFEA)),
        CatProfile("mochi", "Mochi", Color(0xFF2D1B24), Color(0xFFFF9FF3), Color(0xFFFED2FA)),
        CatProfile("siamese", "Siamese", Color(0xFF191A35), Color(0xFF5F27CD), Color(0xA57EFF))
    )
}

class AffirmationRepository {
    suspend fun generateAffirmations(task: String, step: String, hook: String, monster: String): List<String> {
        delay(1800L) // Simulate network/LLM pipeline delay

        val common = listOf(
            "Current Objective: $task.",
            "Your immediate physical target: $step.",
            "Remember, you are doing this to: $hook.",
            "Action precedes momentum. You don't need mental clarity to begin."
        )

        val targeted = when {
            monster.contains("start", ignoreCase = true) -> listOf(
                "Your universe has shrunk to exactly: '$step'. The remainder is completely offline.",
                "Give yourself full absolute clearance to produce a chaotic first attempt."
            )
            monster.contains("boring", ignoreCase = true) -> listOf(
                "Understimulation is uncomfortable, but it cannot physically hold you back.",
                "Ten minutes of tedious execution buys back hours of completely guilt-free freedom."
            )
            else -> listOf(
                "The macroscopic scale is an illusion. Only the immediate grid point is physical.",
                "You do not owe this hour a finished project. You only owe it this individual segment."
            )
        }
        return (common + targeted).shuffled()
    }
}