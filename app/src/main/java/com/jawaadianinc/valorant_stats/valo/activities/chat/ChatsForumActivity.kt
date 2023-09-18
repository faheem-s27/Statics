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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class ChatsForumActivity : Fragment() {

    private lateinit var messagesListView: ListView
    private lateinit var sendMessagesButton: FloatingActionButton
    private lateinit var messageTextBox: EditText
    private lateinit var ChatReference: DatabaseReference
    private lateinit var playerName: String
    private lateinit var playerImage: String
    private lateinit var tabLayout: TabLayout

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
        playerName = activity?.intent?.getStringExtra("playerName") ?: return
        playerImage = activity?.intent?.getStringExtra("playerImageID") ?: return
        tabLayout = requireActivity().findViewById(R.id.tabLayout)

        val playerCardLarge = "https://media.valorant-api.com/playercards/$playerImage/largeart.png"

        val backGround = requireActivity().findViewById(R.id.imageView3) as ImageView
        Picasso.get().load(playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(requireActivity())).into(backGround)


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

        // Assuming you have three tabs for English, Indonesian, and Russian.
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val language = when (tab?.position) {
                    0 -> "English"
                    1 -> "Indonesian"
                    2 -> "Russian"
                    3 -> "Dutch"
                    4 -> "Hungarian"
                    5 -> "Arabic"
                    else -> "English" // Default to English if the position is out of bounds
                }
                ChatReference =
                    FirebaseDatabase.getInstance().reference.child("VALORANT/Chats/$language/")
                ChatReference.keepSynced(true)
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

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Not needed, but you can implement if required
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Not needed, but you can implement if required
            }
        })

        // set the default tab to English
        ChatReference = FirebaseDatabase.getInstance().reference.child("VALORANT/Chats/English/")
        ChatReference.keepSynced(true)
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

        // find what person was clicked on
        messagesListView.setOnItemClickListener { _, _, position, _ ->
            val message = messagesListView.getItemAtPosition(position) as ChatMessage
            val playerName = message.playerMessage
            val playerImage = message.playerImage

            // show a popup with the player's name and image
            val dialog = DialogFragment()
            val dialogView = layoutInflater.inflate(R.layout.chat_dialog, null)
            dialog.dialog?.setContentView(dialogView)
            dialog.dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show(requireActivity().supportFragmentManager, "ChatDialogFragment")

            dialogView.findViewById<TextView>(R.id.textView49).text = playerName
            val imageView = dialogView.findViewById<ImageView>(R.id.imageView10)
            Picasso.get().load(playerImage).fit().centerCrop()
                .into(imageView)

            // dismiss the dialog when the user clicks anywhere
            dialogView.setOnClickListener {
                dialog.dismiss()
            }
        }
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

        val title = "New message from $playerName"
        val encodedStringTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        val encodedStringMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())

        // url to send this to
        val url =
            "https://statics-server.fly.dev/sendNotification?title=${encodedStringTitle}&body=$encodedStringMessage"
        // send the message to the server
        val request = okhttp3.Request.Builder().url(url).build()
        val client = okhttp3.OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            }
        })
    }
}
