package com.scrollthegita.data.content

import com.scrollthegita.core.model.Journey
import org.json.JSONArray
import org.json.JSONException

object JourneyJson {
    const val ASSET_PATH = "journeys.json"

    fun parse(json: String): List<Journey> {
        if (json.isBlank()) return emptyList()
        val array = try {
            JSONArray(json)
        } catch (_: JSONException) {
            return emptyList()
        }
        val journeys = mutableListOf<Journey>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val id = obj.optString("id").trim()
            val title = obj.optString("title").trim()
            val description = obj.optString("description").trim()
            val shlokasArray = obj.optJSONArray("shlokas") ?: continue
            val shlokas = mutableListOf<String>()
            for (j in 0 until shlokasArray.length()) {
                val ref = shlokasArray.optString(j).trim()
                if (ref.isNotEmpty()) {
                    shlokas.add(ref)
                }
            }
            if (id.isNotEmpty() && title.isNotEmpty() && shlokas.isNotEmpty()) {
                journeys.add(
                    Journey(
                        id = id,
                        title = title,
                        description = description,
                        shlokas = shlokas,
                    ),
                )
            }
        }
        return journeys
    }
}
