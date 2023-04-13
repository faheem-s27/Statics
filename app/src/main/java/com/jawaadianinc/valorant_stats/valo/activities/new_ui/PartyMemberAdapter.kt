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

        return row
    }
}
