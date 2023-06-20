package com.jawaadianinc.valorant_stats.valo.activities.new_ui

data class RequestLog(val url: String, val method: String, val dateTime: String, val code: Int, val body: String)
{
    fun getUnixTime() : Long {
        return dateTime.toLong()
    }

}