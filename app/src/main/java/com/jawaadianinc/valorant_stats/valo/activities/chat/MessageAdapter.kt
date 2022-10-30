package com.jawaadianinc.valorant_stats.valo.activities.chat

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class MessageAdapter(
    private var context: Activity,
    private val messages: ArrayList<ChatMessage>
) : ArrayAdapter<Any?>(
    context, R.layout.messages_layout, messages as List<Any?>
) {
    private var wasPlayed = false

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.messages_layout, null, true)

        val message = messages[position]
        val chatProfile = row!!.findViewById<View>(R.id.chatPlayerProfile) as ImageView
        val chatMessage = row.findViewById<View>(R.id.chatPlayerText) as TextView
        val chatName = row.findViewById<View>(R.id.chatPlayerName) as TextView
        val chatTime = row.findViewById<View>(R.id.chatMessageTime) as TextView

        Picasso
            .get()
            .load(message.playerImage)
            .fit()
            .into(chatProfile)

        chatMessage.text = message.playerMessage
        chatName.text = message.playerName
        chatTime.text = message.getDateFormatted()

        if (position == messages.size - 1) {
            // check if it was already played
            if (!wasPlayed) {
                row.alpha = 0f
                // animate them coming in from the side
                row.translationX = -250f
                row.animate().translationXBy(250f).setDuration(500).alpha(1f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.start()
                wasPlayed = true
            }
        }

        return row
    }

}
