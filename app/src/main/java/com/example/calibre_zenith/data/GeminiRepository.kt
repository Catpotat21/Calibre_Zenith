package com.example.calibre_zenith.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiRepository {

    // Configure the model to enforce JSON outputs strictly
    private val config = generationConfig {
        responseMimeType = "application/json"
        temperature = 0.2f // Low temperature for high instruction adherence
    }

    // Replace with your secure key management setup (e.g., BuildConfig.GEMINI_KEY)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "YOUR_API_KEY_HERE",
        generationConfig = config
    )

    suspend fun fetchReframedDirectives(
        taskName: String,
        microStep: String,
        monster: String
    ): CognitiveResponse = withContext(Dispatchers.IO) {
        val systemPersona = """
            You are the elite, minimalist, no-nonsense executive performance engine of Calibre Zenith.
            Your job is to break starting friction by deconstructing a daunting task and providing 
            custom psychological reframing affirmations tailored specifically to a person's inner resistance.
            
            Return exactly a JSON object matching this schema:
            {
              "deconstructedStep": "A sharp, bite-sized, 60-second tactical action that bypasses the friction.",
              "affirmations": ["An array of exactly 5 short, impactful, punchy uppercase statements in a stoic tone."]
            }
        """.trimIndent()

        val structuredPrompt = """
            $systemPersona
            
            Current User Session Diagnostics:
            - Target Objective: $taskName
            - Core Psychological Resistance Profile: $monster
            - User's Proposed First Action: $microStep
        """.trimIndent()

        return@withContext try {
            val response = generativeModel.generateContent(structuredPrompt)
            val jsonText = response.text ?: throw Exception("Empty stream received from Gemini API.")

            val jsonObject = JSONObject(jsonText)
            val deconstructed = jsonObject.optString("deconstructedStep", microStep)
            val affirmationsArray = jsonObject.optJSONArray("affirmations")

            val extractedAffirmations = mutableListOf<String>()
            if (affirmationsArray != null) {
                for (i in 0 until affirmationsArray.length()) {
                    extractedAffirmations.add(affirmationsArray.getString(i))
                }
            }

            CognitiveResponse(
                deconstructedStep = deconstructed,
                affirmations = extractedAffirmations.ifEmpty { getDefaultFallbackAffirmations() }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback so network/API errors never break user application state
            CognitiveResponse(
                deconstructedStep = microStep,
                affirmations = getDefaultFallbackAffirmations()
            )
        }
    }

    private fun getDefaultFallbackAffirmations(): List<String> {
        return listOf(
            "ACTION PRECEDES MOMENTUM. YOU DO NOT NEED CLARITY TO BEGIN.",
            "YOUR UNIVERSE HAS SHRUNK TO EXACTLY THIS STEP. THE REST IS OFFLINE.",
            "GIVE YOURSELF ABSOLUTE CLEARANCE TO PRODUCE A CHAOTIC FIRST ATTEMPT.",
            "UNDERSTIMULATION IS TEMPORARY DISCOMFORT. IT CANNOT HOLD YOU BACK.",
            "YOU DO NOT OWE THIS HOUR A FINISHED PROJECT. JUST THIS SEGMENT."
        )
    }
}

// Data container for parsed response
data class CognitiveResponse(
    val deconstructedStep: String,
    val affirmations: List<String>
)