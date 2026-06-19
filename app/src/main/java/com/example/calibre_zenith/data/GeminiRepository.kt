package com.example.calibre_zenith.data

import com.example.calibre_zenith.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

class GeminiRepository {

    // Verified AQ-signature security token for Google AI Studio gateway handshakes
    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content {
            text("""
                You are a hyper-focused cognitive reframing system engineered for software developers and researchers facing executive dysfunction, task inertia, or perfectionism paralysis.
                Your objective is to ingest an engineering objective, a 60-second mechanical trigger action, and a specific cognitive resistance profile, then output exactly three ultra-grounded, tactile, actionable tactical directives.
                
                CRITICAL SYSTEM RULES:
                1. REJECT all forms of toxic positivity, generic cheerleading, or ambiguous motivational idioms.
                2. Use clear, objective, and slightly clinical machine prose.
                3. Address physical and mental grounding. Break down complex operational overhead into raw immediate components.
                4. Return exactly 3 directives inside a strict JSON object structure matching this explicit schema: {"directives": ["string1", "string2", "string3"]}
            """.trimIndent())
        }
    )

    suspend fun fetchReframedDirectives(taskName: String, microStep: String, monster: String): List<String> {
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API Engine failure: API key credentials not detected in project structure.")
        }

        val prompt = """
            Target Task: $taskName
            Immediate 60-second Action: $microStep
            Cognitive Resistance Friction Profile: $monster
        """.trimIndent()

        // Dispatch async handshake payload to the generative core engine
        val response = model.generateContent(content {
            text(prompt)
        })

        val responseText = response.text ?: throw Exception("Generative SDK returned an empty string pipeline response.")

        // BULLETPROOF EXTRACTION: Locates the true structural JSON payload boundaries,
        // completely ignoring markdown code block indicators or accidental pre/post text.
        val startIndex = responseText.indexOf('{')
        val endIndex = responseText.lastIndexOf('}')

        if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
            throw Exception("Invalid JSON payload structure returned from core engine. Raw response: $responseText")
        }

        val cleanedJson = responseText.substring(startIndex, endIndex + 1)

        // Parse structured data matrix
        val jsonObject = JSONObject(cleanedJson)
        val jsonArray = jsonObject.getJSONArray("directives")

        val directivesList = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            directivesList.add(jsonArray.getString(i))
        }

        return directivesList
    }
}