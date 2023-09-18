package com.jawaadianinc.valorant_stats.valo.activities.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatMessage(
    val playerName: String,
    val playerImage: String,
    val playerMessage: String,
    val unixTime: Long
) {
    // method to get the current time in a string format to be displayed in the chat like "03:00 PM 27th April 2022" using the unix time provided
    fun getDateFormatted(): String {
        val currentTime = Date(unixTime)
        val timeRightNow = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val dateFormat2 = SimpleDateFormat("hh:mm dd MMMM yyyy", Locale.getDefault())
        val dateFormat3 = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val currentDate = dateFormat3.format(currentTime)
        val rightNowDate = dateFormat3.format(timeRightNow)

        return when (currentDate) {
            rightNowDate -> dateFormat.format(currentTime)
            dateFormat3.format(Date(System.currentTimeMillis() - 86400000)) ->
                "Yesterday at ${dateFormat.format(currentTime)}"

            else -> dateFormat2.format(currentTime)
        }
    }
}
