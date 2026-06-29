package com.example.calibre_zenith.data

import android.content.Context
import androidx.compose.runtime.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class TaskNode(
    val id: String = UUID.randomUUID().toString(),
    initialTitle: String,
    initialDetails: String = "",
    initialIsCompleted: Boolean = false,
    initialScheduledDate: String? = null,
    initialScheduledTime: String? = null,
    initialScheduledEndTime: String? = null,
    initialFlairs: List<String> = emptyList(),
    initialHpDrain: Int = 10,            // ← NEW
    initialAssignedBossId: Int? = null   // ← NEW
) {
    var title by mutableStateOf(initialTitle)
    var details by mutableStateOf(initialDetails)
    var isCompleted by mutableStateOf(initialIsCompleted)
    var scheduledDate by mutableStateOf(initialScheduledDate)
    var scheduledTime by mutableStateOf(initialScheduledTime)
    var scheduledEndTime by mutableStateOf(initialScheduledEndTime)
    val flairs = mutableStateListOf<String>().apply { addAll(initialFlairs) }
    val children = mutableStateListOf<TaskNode>()
    var hpDrain by mutableStateOf(initialHpDrain)              // ← NEW
    var assignedBossId by mutableStateOf(initialAssignedBossId) // ← NEW
}

object RoadmapPersistence {

    private fun nodeToJson(node: TaskNode): JSONObject {
        val json = JSONObject()
        json.put("id", node.id)
        json.put("title", node.title)
        json.put("details", node.details)
        json.put("isCompleted", node.isCompleted)
        json.put("scheduledDate", node.scheduledDate ?: JSONObject.NULL)
        json.put("scheduledTime", node.scheduledTime ?: JSONObject.NULL)
        json.put("scheduledEndTime", node.scheduledEndTime ?: JSONObject.NULL)
        json.put("hpDrain", node.hpDrain)
        json.put("assignedBossId", node.assignedBossId ?: JSONObject.NULL)
        val flairsArray = JSONArray()
        node.flairs.forEach { flairsArray.put(it) }
        json.put("flairs", flairsArray)
        val childrenArray = JSONArray()
        node.children.forEach { childrenArray.put(nodeToJson(it)) }
        json.put("children", childrenArray)
        return json
    }

    private fun jsonToNode(json: JSONObject): TaskNode {
        val node = TaskNode(
            id = json.optString("id", UUID.randomUUID().toString()),
            initialTitle = json.optString("title", ""),
            initialDetails = json.optString("details", ""),
            initialIsCompleted = json.optBoolean("isCompleted", false),
            initialScheduledDate = if (json.isNull("scheduledDate")) null else json.optString("scheduledDate"),
            initialScheduledTime = if (json.isNull("scheduledTime")) null else json.optString("scheduledTime"),
            initialScheduledEndTime = if (json.isNull("scheduledEndTime")) null else json.optString("scheduledEndTime"),
            initialHpDrain = json.optInt("hpDrain", 10),
            initialAssignedBossId = if (json.isNull("assignedBossId")) null else json.optInt("assignedBossId"),
            initialFlairs = mutableListOf<String>().apply {
                val arr = json.optJSONArray("flairs")
                if (arr != null) for (i in 0 until arr.length()) add(arr.getString(i))
            }
        )
        val childrenArray = json.optJSONArray("children")
        if (childrenArray != null) {
            for (i in 0 until childrenArray.length()) {
                node.children.add(jsonToNode(childrenArray.getJSONObject(i)))
            }
        }
        return node
    }

    private fun recursiveExtractScheduled(node: TaskNode, list: MutableList<TaskNode>) {
        if (!node.scheduledDate.isNullOrBlank()) list.add(node)
        node.children.forEach { recursiveExtractScheduled(it, list) }
    }

    fun saveRoadmap(context: Context, nodes: List<TaskNode>) {
        try {
            val file = File(context.filesDir, "roadmap_data.json")
            val rootArray = JSONArray()
            nodes.forEach { rootArray.put(nodeToJson(it)) }
            file.writeText(rootArray.toString())

            val scheduledList = mutableListOf<TaskNode>()
            nodes.forEach { recursiveExtractScheduled(it, scheduledList) }
            val plannerFile = File(context.filesDir, "planner_scheduled_tasks.json")
            val plannerArray = JSONArray()
            for (task in scheduledList) {
                val obj = JSONObject()
                obj.put("id", task.id)
                obj.put("title", task.title)
                obj.put("details", task.details)
                obj.put("scheduledDate", task.scheduledDate)
                obj.put("scheduledTime", task.scheduledTime ?: "")
                obj.put("scheduledEndTime", task.scheduledEndTime ?: "")
                obj.put("isCompleted", task.isCompleted)
                plannerArray.put(obj)
            }
            plannerFile.writeText(plannerArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadRoadmap(context: Context): List<TaskNode> {
        val nodes = mutableListOf<TaskNode>()
        try {
            val file = File(context.filesDir, "roadmap_data.json")
            if (file.exists()) {
                val content = file.readText()
                if (content.isNotBlank()) {
                    val rootArray = JSONArray(content)
                    for (i in 0 until rootArray.length()) {
                        nodes.add(jsonToNode(rootArray.getJSONObject(i)))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return nodes
    }
}