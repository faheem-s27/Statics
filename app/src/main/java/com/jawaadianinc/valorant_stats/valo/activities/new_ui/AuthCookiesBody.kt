package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import com.google.gson.annotations.SerializedName

data class AuthCookiesBody(
    @SerializedName("client_id") val clientId: String = "play-valorant-web-prod",
    @SerializedName("nonce") val nonce: String = "1",
    @SerializedName("redirect_uri") val redirectUri: String = "https://playvalorant.com/opt_in",
    @SerializedName("response_type") val responseType: String = "token id_token",
    @SerializedName("response_mode") val responseMode: String = "query",
    @SerializedName("scope") val scope: String = "account openid"
)
