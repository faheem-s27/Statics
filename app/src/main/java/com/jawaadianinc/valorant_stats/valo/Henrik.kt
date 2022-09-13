package com.jawaadianinc.valorant_stats.valo

import android.content.Context
import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class Henrik(val context: Context) {

    fun henrikAPI(playerURL: String): JSONObject {
        return executeRequest(playerURL)
    }

    private fun executeRequest(playerURL: String): JSONObject {
        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/henrikCalls")
        val playerName = PlayerDatabase(context).getPlayerName()
        val time = System.currentTimeMillis()
        playersRef.child(playerName!!.split("#")[0]).child("Calls").child(time.toString())
            .setValue(playerURL)

        val ze_key = database.getReference("VALORANT/henrik")
        // get the value of the key
        var key = ""
        ze_key.get().addOnSuccessListener {
            key = it.value.toString()
            //Log.d("Henrik", "Key: $key")
        }.addOnFailureListener {
            Log.d("Henrik", "Failed to get key")
        }

        val client = OkHttpClient()
        val urlBuilder: HttpUrl.Builder =
            playerURL.toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "HDEV-67e86af9-8bf9-4f6d-b628-f4521b20d772")
            .build()
        val call = client.newCall(request).execute()
        // Log the headers
        //Log.d("Henrik", "Headers: ${call.headers}")
        return JSONObject(call.body.string())
    }
}
