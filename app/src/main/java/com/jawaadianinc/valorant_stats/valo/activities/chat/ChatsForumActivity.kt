package com.jawaadianinc.valorant_stats.valo.activities.chat

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityChatsForumBinding
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

class ChatsForumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatsForumBinding
    private lateinit var messagesListView: ListView
    private lateinit var sendMessagesButton: FloatingActionButton
    private lateinit var messageTextBox: EditText
    private lateinit var ChatReference: DatabaseReference
    private lateinit var playerName: String
    private lateinit var playerImage: String
    private lateinit var speakerText: TextView
    private lateinit var speakerImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsForumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.materialToolbar2
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Statics Chat"
        toolbar.setNavigationOnClickListener {
            // finish and override the transition
            finish()
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        sendMessagesButton = binding.sendMessageFAB
        messageTextBox = binding.sendMesssageEditText
        messagesListView = binding.messagesListView
        ChatReference = FirebaseDatabase.getInstance().reference.child("VALORANT/Chats/")
        playerName = intent.getStringExtra("playerName").toString()
        playerImage = intent.getStringExtra("playerImage").toString()

        val playerCardSmall = "https://media.valorant-api.com/playercards/$playerImage/smallart.png"
        val playerCardLarge = "https://media.valorant-api.com/playercards/$playerImage/largeart.png"

        val backGround = binding.imageView3
        Picasso.get().load(playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(this)).into(backGround)

        speakerText = binding.chatSpeaker
        speakerText.text = "Speaking as: $playerName"
        speakerImage = binding.chatSpeakerIcon
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
            } else if (message.length > 1000) {
                Toast.makeText(this, "Message is too long", Toast.LENGTH_SHORT).show()
            }
        }

        // if the user clicks enter, send the message
        messageTextBox.setOnEditorActionListener { _, _, _ ->
            val message = messageTextBox.text.toString()
            if (message.isNotEmpty() && message.length < 1000) {
                sendMessage(message)
                // Clear the text box
                messageTextBox.setText("")
            } else if (message.length > 1000) {
                Toast.makeText(this, "Message is too long", Toast.LENGTH_SHORT).show()
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
                    this@ChatsForumActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateMessages() {
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
                }
            }
            // add the messages to the list view
            val adapter = MessageAdapter(this, messages)
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

        // create unique id for the message
        val messageId = ChatReference.push().key.toString()
        ChatReference.child(messageId).child("playerName").setValue(playerName)
        ChatReference.child(messageId).child("playerImage").setValue(playerImage)
        ChatReference.child(messageId).child("playerMessage").setValue(message)
        ChatReference.child(messageId).child("unixTime").setValue(unixTime)

        // update the messages
        updateMessages()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }
}
