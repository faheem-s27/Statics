package com.jawaadianinc.valorant_stats.valo.activities.new_ui

data class PreGameAgentSelectPlayer(val name: String, val agentSelectState: String, val agentID: String?, val rankImage: String?)
{
    fun getAgentImage(): String {
        return "https://media.valorant-api.com/agents/${agentID}/displayicon.png"
    }
}