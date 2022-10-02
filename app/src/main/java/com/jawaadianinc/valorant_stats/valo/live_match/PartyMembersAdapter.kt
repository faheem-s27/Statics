package com.jawaadianinc.valorant_stats.valo.live_match

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class PartyMembersAdapter(
    private val context: Activity,
    private val partyMembers: ArrayList<PartyMember>,
) : ArrayAdapter<Any?>(
    context, R.layout.voice_line, partyMembers as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView = convertView
        val inflater = context.layoutInflater
        if (convertView == null) rowView = inflater.inflate(R.layout.party_player_row, null, true)

        val partyMember = partyMembers[position]

        val cardImage = rowView?.findViewById<View>(R.id.PartyMemberImage) as ImageView
        Picasso.get().load(partyMember.card).into(cardImage)

        val title = rowView.findViewById<View>(R.id.PartyMemberTitle) as TextView
        title.text = partyMember.title

        val level = rowView.findViewById<View>(R.id.PartyMemberLevel) as TextView
        level.text = "Level " + partyMember.level.toString()

        return rowView
    }
}
