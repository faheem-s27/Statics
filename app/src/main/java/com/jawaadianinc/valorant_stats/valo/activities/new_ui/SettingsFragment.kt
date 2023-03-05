package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase

class SettingsFragment : Fragment() {
    lateinit var playerName: String

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

    }

    fun logOut(name: String) {
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

}
