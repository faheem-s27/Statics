package com.jawaadianinc.valorant_stats.valo.activities.chat

import java.text.SimpleDateFormat
import java.util.*

class ChatMessage(
    val playerName: String,
    val playerImage: String,
    val playerMessage: String,
    val unixTime: Long
) {
    // method to get the current time in a string format to be displayed in the chat like "03:00 PM 27th April 2022" using the unix time provided
    fun getDateFormatted(): String {
        val currentTime = Date(unixTime)
        val dateFormat = SimpleDateFormat("hh:mm a dd MMMM yyyy", Locale.getDefault())
        return dateFormat.format(currentTime)
    }
}
