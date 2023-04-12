package com.jawaadianinc.valorant_stats.valo.activities.new_ui

data class AuthRequestBody(
    val username: String,
    val password: String
) {
    val type: String = "auth"
    val language: String = "en_US"
    val remember: Boolean = true
}
