package com.jawaadianinc.valorant_stats.valo.activities.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

class ChatsForumActivity : Fragment() {

    private lateinit var messagesListView: ListView
    private lateinit var sendMessagesButton: FloatingActionButton
    private lateinit var messageTextBox: EditText
    private lateinit var ChatReference: DatabaseReference
    private lateinit var playerName: String
    private lateinit var playerImage: String
    private lateinit var speakerText: TextView
    private lateinit var speakerImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_chats_forum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendMessagesButton = requireActivity().findViewById(R.id.sendMessageFAB)
        messageTextBox = requireActivity().findViewById(R.id.sendMesssageEditText)
        messagesListView = requireActivity().findViewById(R.id.messagesListView)
        ChatReference = FirebaseDatabase.getInstance().reference.child("VALORANT/Chats/")
        playerName = activity?.intent?.getStringExtra("playerName") ?: return
        playerImage = activity?.intent?.getStringExtra("playerImageID") ?: return

        val playerCardSmall = "https://media.valorant-api.com/playercards/$playerImage/smallart.png"
        val playerCardLarge = "https://media.valorant-api.com/playercards/$playerImage/largeart.png"

        val backGround = requireActivity().findViewById(R.id.imageView3) as ImageView
        Picasso.get().load(playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(requireActivity())).into(backGround)

        speakerText = requireActivity().findViewById(R.id.chat_Speaker)
        speakerText.text = "${getString(R.string.s174)}: $playerName"
        speakerImage = requireActivity().findViewById(R.id.chat_Speaker_Icon)
        Picasso
            .get()
            .load(playerCardSmall)
            .fit()
            .into(speakerImage)

        sendMessagesButton.setOnClickListener {
            val message = messageTextBox.text.toString()
            if (message.isNotEmpty() && message.length < 1000) {
                sendMessage(message)
                // Clear the text box
                messageTextBox.setText("")
            } else if (message.length > 500) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.message_exceeds_500_characters), Toast.LENGTH_SHORT
                ).show()
            }
        }

        // if the user clicks enter, send the message
        messageTextBox.setOnEditorActionListener { _, _, _ ->
            val message = messageTextBox.text.toString()
            if (message.isNotEmpty() && message.length < 1000) {
                sendMessage(message)
                // Clear the text box
                messageTextBox.setText("")
            } else if (message.length > 500) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.message_exceeds_500_characters),
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }

        // listen for changes in the database and update the messages
        ChatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateMessages()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireActivity(),
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateMessages() {
        if (context == null) return
        val messages = ArrayList<ChatMessage>()
        val messagesFromDatabase = ChatReference.get()
        messagesFromDatabase.addOnSuccessListener {
            val messagesFromDatabase = it.children
            messagesFromDatabase.forEach { message ->
                // get the message
                try {
                    val messageText = message.child("playerMessage").value.toString()
                    val messageSender = message.child("playerName").value.toString()
                    val messageSenderImage = message.child("playerImage").value.toString()
                    val unix = message.child("unixTime").value.toString()

                    // check if any of the fields are empty
                    if (messageText.isNotEmpty() && messageSender.isNotEmpty() && messageSenderImage.isNotEmpty() && unix.isNotEmpty()) {
                        messages.add(
                            ChatMessage(
                                messageSender,
                                messageSenderImage,
                                messageText,
                                unix.toLong()
                            )
                        )
                    }
                } catch (e: Exception) {
                    // send to firebase
                    FirebaseDatabase.getInstance().reference.child("Statics/Errors/").child("Chat")
                        .setValue(e.toString())
                    Log.d("StaticsChats", "Error: ${e.message}")
                }
            }
            // add the messages to the list view
            if (context == null) return@addOnSuccessListener
            val adapter = MessageAdapter(requireActivity(), messages)
            // sort the messages by time
            messages.sortBy { it.unixTime }
            messagesListView.adapter = adapter

            // scroll to the bottom of the list view
            messagesListView.setSelection(messagesListView.count - 1)
        }
    }

    private fun sendMessage(message: String) {
        // get the current time
        val unixTime = System.currentTimeMillis()

        val playerCardSmall = "https://media.valorant-api.com/playercards/$playerImage/smallart.png"

        if (context == null) return
        // create unique id for the message
        val messageId = ChatReference.push().key.toString()
        ChatReference.child(messageId).child("playerName").setValue(playerName)
        ChatReference.child(messageId).child("playerImage").setValue(playerCardSmall)
        ChatReference.child(messageId).child("playerMessage").setValue(message)
        ChatReference.child(messageId).child("unixTime").setValue(unixTime)

        // update the messages
        updateMessages()
    }
}
