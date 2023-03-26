package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valo.activities.chat.ChatsForumActivity
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

class SettingsFragment : Fragment() {
    lateinit var playerName: String
    var playerImage: String = ""
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerName = activity?.intent?.getStringExtra("playerName") ?: return
        val logOutButton = view.findViewById<View>(R.id.new_SignOutButton)
        logOutButton.setOnClickListener {
            // Add a confirmation dialog
            val alert = android.app.AlertDialog.Builder(requireActivity())
            alert.setTitle("Log out")
            alert.setMessage("Are you sure you want to log out?")
            alert.setPositiveButton("Yes") { _, _ ->
                logOut(playerName.split("#")[0])
            }
            alert.setNegativeButton("No") { _, _ -> }
            alert.show()
        }

        gettingPlayerProfile()


        val ChatsButton = view.findViewById<FloatingActionButton>(R.id.new_ExtrasChatButton)
        ChatsButton.setOnClickListener {
            // get the player image from the fragments activity
            playerImage = StaticsMainActivity.playerImage
            if (playerImage == "") {
                // Tell the user to wait
                Toast.makeText(requireActivity(), "Please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(requireActivity(), ChatsForumActivity::class.java)
            intent.putExtra("playerName", playerName)
            intent.putExtra("playerImage", playerImage)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

    }

    private fun logOut(name: String) {
        val playerDB = PlayerDatabase(requireActivity())
        if (playerDB.logOutPlayer(name)) {
            val widgetIntent = Intent(requireActivity(), LastMatchWidget::class.java)
            widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(activity?.application).getAppWidgetIds(
                ComponentName(activity?.applicationContext!!, LastMatchWidget::class.java)
            )
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            activity?.sendBroadcast(widgetIntent)

            startActivity(Intent(requireActivity(), LoggingInActivityRSO::class.java))
            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            activity?.finish()
            Toast.makeText(requireActivity(), "Logged out!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireActivity(), "Error logging out O_o", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gettingPlayerProfile() {
        val name = view?.findViewById<TextView>(R.id.Extras_PlayerName)
        name?.text = playerName
        if (timer != null) {
            timer?.cancel()
        }
        Log.d("settingsFragment", "gettingPlayerProfile: $playerImage")
        playerImage = StaticsMainActivity.playerImage
        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Check if the player image is not empty
                playerImage = StaticsMainActivity.playerImage
                if (playerImage != "" && StaticsMainActivity.largeplayerImage != "") {
                    // Get the player image
                    val pfp = view?.findViewById<ImageView>(R.id.Extras_PlayerPFP)
                    val Lpfp = view?.findViewById<ImageView>(R.id.Extras_LargePlayerPFP)
                    // Set the player image
                    Picasso.get().load(playerImage).fit().centerCrop().into(pfp)
                    Picasso.get().load(StaticsMainActivity.largeplayerImage).fit().centerCrop()
                        .transform(BlurTransformation(requireContext())).into(Lpfp)
                    // Stop the timer
                    timer?.cancel()
                }
            }

            override fun onFinish() {
                // Do nothing
                timer?.cancel()
            }
        }

        (timer as CountDownTimer).start()
    }

}
