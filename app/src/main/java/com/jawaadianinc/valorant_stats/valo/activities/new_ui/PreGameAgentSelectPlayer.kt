package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.json.JSONObject

data class PreGameAgentSelectPlayer(val name: String, val agentSelectState: String, val agentID: String, val rankImage: String, val agentName: String)
{
    fun getAgentImage(): String {
        return "https://media.valorant-api.com/agents/${agentID}/displayicon.png"
    }
}