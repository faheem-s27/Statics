package com.jawaadianinc.valorant_stats.main

data class Data(
    val account_level: Int,
    val card: Card,
    val last_update: String,
    val last_update_raw: Int,
    val name: String,
    val puuid: String,
    val region: String,
    val tag: String
)
