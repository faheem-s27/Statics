package com.jawaadianinc.valorant_stats.valo.activities.new_ui

data class CoreGamePlayer(val name: String, val agentName: String, val agentID: String, val rankImage: String, val team: String) {
    fun getAgentImage(): String {
        return "https://media.valorant-api.com/agents/${agentID}/displayicon.png"
    }
}