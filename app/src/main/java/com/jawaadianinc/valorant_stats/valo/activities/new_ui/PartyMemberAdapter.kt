package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.json.JSONObject

class PartyMemberAdapter(
    private var context: Activity,
    private val members: ArrayList<PartyMember>
) :
    ArrayAdapter<Any?>(
        context, R.layout.live_mode_party_member, members as List<Any?>
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.live_mode_party_member, null, true)

        val member = members[position]
        val memberProfile = row!!.findViewById<ImageView>(R.id.new_playerAvatar) as ImageView
        val memberGamename = row.findViewById<TextView>(R.id.new_partyPlayerName) as TextView
        val memberTitle = row.findViewById<TextView>(R.id.new_partyPlayerTitle) as TextView
        val memberGameTag = row.findViewById<TextView>(R.id.new_partyPlayerTag) as TextView
        val memberStatus = row.findViewById<TextView>(R.id.new_playerPartyStatus) as TextView

        Picasso
            .get()
            .load(member.getPlayerImage())
            .fit()
            .centerInside()
            .into(memberProfile)

        memberGamename.text = member.gameName
        memberGameTag.text = "#${member.gameTag}"
        memberTitle.text = member.getTitle(context)
        if (member.isReady) {
            memberStatus.text = "Ready"
            memberStatus.setTextColor(Color.parseColor("#00FF00"))
        } else {
            memberStatus.text = "Not Ready"
            memberStatus.setTextColor(Color.parseColor("#FF0000"))
        }

        val rankImage = row.findViewById(R.id.new_playerRank) as ImageView

        Picasso
            .get()
            .load(getRank(member.gameName, member.gameTag, member.region))
            .fit()
            .centerInside()
            .into(rankImage)

        return row
    }

    private fun getRank(name: String, tag: String, region: String): String {
        val rankPreferences = context.getSharedPreferences("rank", 0)

        // check when was the last time we updated the rank
        val lastUpdated = rankPreferences.getLong("lastUpdated", 0)
        if (System.currentTimeMillis() - lastUpdated > 1000 * 60 * 60) {
            rankPreferences.edit().clear().apply()
            rankPreferences.edit().putLong("lastUpdated", System.currentTimeMillis()).apply()
        }

        val rank = rankPreferences.getString("$name#$tag", "")
        if (rank != "") return rank!!

        val client = okhttp3.OkHttpClient()
        val url = "https://api.henrikdev.xyz/valorant/v2/mmr/$region/$name/$tag"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "HDEV-67e86af9-8bf9-4f6d-b628-f4521b20d772")
            .build()
        return runBlocking(Dispatchers.IO)
        {
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body.string())
            val rank = json.getJSONObject("data")
                .getJSONObject("current_data")
                .getJSONObject("images")
                .getString("large")
            rankPreferences.edit().putString("$name#$tag", rank).apply()
            return@runBlocking rank
        }
    }
}
