package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import com.google.gson.annotations.SerializedName

data class ClientPlatformBody(
    @SerializedName("platformType") val platformType: String = "PC",
    @SerializedName("platformOS") val platformOS: String = "Windows",
    @SerializedName("platformOSVersion") val platformOSVersion: String = "10.0.19042.1.256.64bit",
    @SerializedName("platformChipset") val platformBrowser: String = "Unknown",
    )
