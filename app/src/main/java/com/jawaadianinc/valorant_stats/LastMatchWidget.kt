package com.jawaadianinc.valorant_stats

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.main.SplashActivity
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.jawaadianinc.valorant_stats.valo.databases.MatchDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class LastMatchWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.last_match_widget)

    // get current time in hh:mm:ss format
    val currentTime = Calendar.getInstance().time
    val time = currentTime.toString().split(" ")[3].split(":")
    val hour = time[0]
    val minute = time[1]
    val currentTimeString = "$hour:$minute"

    // set the textview to the current time
    views.setTextViewText(R.id.appwidget_widgetUpdate, "Last update: $currentTimeString")

    val playerName: String? = PlayerDatabase(context).getPlayerName()
    // split the player name into first and last name from #

    // check if null
    if (playerName == null) {
        views.setTextViewText(R.id.appwidget_KDA, "Tap to sign in")

        val intent = Intent(context, SplashActivity::class.java)

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, intent, 0)
        }

        views.setOnClickPendingIntent(R.id.appwidget_mapImage, pendingIntent)

        views.setImageViewBitmap(R.id.appwidget_mapImage, null)
        views.setViewVisibility(R.id.appwidget_timePlayed, View.GONE)
        views.setImageViewBitmap(R.id.appwidget_agentImage, null)
        views.setViewVisibility(R.id.appwidget_gameMode, View.GONE)
    } else {
        val matchJSON = MatchDatabase(context).checkForAnyMatches()

        val name = playerName.split("#")

        // check if any matches are present
        if (matchJSON == null) { // user hasnt enabled widgets
            views.setViewVisibility(R.id.appwidget_KDA, View.VISIBLE)
            views.setTextViewText(R.id.appwidget_KDA, "Tap to enable widget")

            val intent = Intent(context, SplashActivity::class.java)
            val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(context, 0, intent, 0)
            }
            views.setOnClickPendingIntent(R.id.appwidget_mapImage, pendingIntent)
            views.setImageViewBitmap(R.id.appwidget_mapImage, null)
            views.setViewVisibility(R.id.appwidget_timePlayed, View.GONE)
            views.setImageViewBitmap(R.id.appwidget_agentImage, null)
            views.setViewVisibility(R.id.appwidget_gameMode, View.GONE)
        } else { // user has enabled widgets
            try {
                views.setViewVisibility(R.id.appwidget_KDA, View.VISIBLE)
                views.setViewVisibility(R.id.appwidget_timePlayed, View.VISIBLE)
                views.setViewVisibility(R.id.appwidget_gameMode, View.VISIBLE)

                val jsonMatch = JSONObject(matchJSON).getJSONObject("data")
                val metadata = jsonMatch.getJSONObject("metadata")
                val map = metadata.getString("map")
                val mode = metadata.getString("mode")
                val matchID = metadata.getString("matchid")

                views.setTextViewText(R.id.appwidget_gameMode, mode)


                val unixTimeStart = metadata.getInt("game_start")
                val date = Date(unixTimeStart * 1000L)
                val d: Duration =
                    Duration.between(
                        date.toInstant(),
                        Instant.now()
                    )

                val timeinDays = d.toDays()
                val timeInHours = d.toHours()
                when {
                    timeinDays > 0 -> {
                        // if it is 1 day then dont add the s, else add the s to days
                        if (timeinDays == 1L) {
                            views.setTextViewText(
                                R.id.appwidget_timePlayed,
                                "$timeinDays day ago"
                            )
                        } else {
                            views.setTextViewText(
                                R.id.appwidget_timePlayed,
                                "$timeinDays days ago"
                            )
                        }
                    }
                    timeInHours > 0 -> {

                        // if it is 1 hour then dont add the s, else add the s to hours
                        if (timeInHours == 1L) {
                            views.setTextViewText(
                                R.id.appwidget_timePlayed,
                                "$timeInHours hour ago"
                            )
                        } else {
                            views.setTextViewText(
                                R.id.appwidget_timePlayed,
                                "$timeInHours hours ago"
                            )
                        }

                    }
                    else -> {
                        // if it is 1 minute then dont add the s, else add the s to minutes
                        if (d.toMinutes() == 1L) {
                            views.setTextViewText(
                                R.id.appwidget_timePlayed,
                                "${d.toMinutes()} minute ago"
                            )
                        } else {
                            views.setTextViewText(
                                R.id.appwidget_timePlayed,
                                "${d.toMinutes()} minutes ago"
                            )
                        }

                    }
                }

                var kda = ""

                val allPlayers = jsonMatch.getJSONObject("players").getJSONArray("all_players")
                for (i in 0 until allPlayers.length()) {
                    val player = allPlayers.getJSONObject(i)
                    if (player.getString("name") == playerName.split("#")[0]) {
                        val playerStats = player.getJSONObject("stats")
                        val kills = playerStats.getInt("kills")
                        val deaths = playerStats.getInt("deaths")
                        val assists = playerStats.getInt("assists")
                        kda = "$kills/$deaths/$assists"

                        // get agent name
                        val agent = player.getString("character")
                        // get agent image
                        val agentImage = AssetsDatabase(context).retrieveImage(agent)

                        // remove all black pixels from image
                        val agentImageNoBlack = agentImage.copy(Bitmap.Config.ARGB_8888, true)
                        for (x in 0 until agentImageNoBlack.width) {
                            for (y in 0 until agentImageNoBlack.height) {
                                val pixel = agentImageNoBlack.getPixel(x, y)
                                if (pixel == -16777216) {
                                    // set pixel to transparent
                                    agentImageNoBlack.setPixel(x, y, 0)
                                }
                            }
                        }

                        views.setImageViewBitmap(R.id.appwidget_agentImage, agentImageNoBlack)
                        break
                    }
                }


                // get the map image from database
                val mapImage = AssetsDatabase(context).retrieveImage(map)

                // set the bitmap to the imageview
                views.setImageViewBitmap(R.id.appwidget_mapImage, mapImage)
                views.setTextViewText(R.id.appwidget_KDA, kda)

                val intent = Intent(context, MatchHistoryActivity::class.java)
                intent.putExtra("RiotName", name[0])
                intent.putExtra("RiotTag", name[0])
                intent.putExtra("MatchNumber", 0)
                intent.putExtra("MatchID", matchID)
                val pendingIntent: PendingIntent =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
                    } else {
                        PendingIntent.getActivity(context, 0, intent, 0)
                    }
                views.setOnClickPendingIntent(R.id.appwidget_mapImage, pendingIntent)

                val database = Firebase.database
                val statusRef = database.getReference("VALORANT/widgetStatus")

                // get the name of the user and the last time the widget was updated, and update the database
                val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
                statusRef.child(name[0]).child("Last updated")
                    .setValue(currentTimeString + " " + dateFormat.format(Date()))

            } catch (e: Exception) {
                views.setTextViewText(
                    R.id.appwidget_KDA,
                    "Error: Invalid data!\nPlease restart the app or widget."
                )

                val database = Firebase.database
                val errorsRef = database.getReference("VALORANT/widgetErrors")

                val error = hashMapOf(
                    "error" to e.toString(),
                    "player" to name[0]
                )

                errorsRef.push().setValue(error)

                views.setImageViewBitmap(R.id.appwidget_mapImage, null)
                views.setViewVisibility(R.id.appwidget_timePlayed, View.GONE)
                views.setImageViewBitmap(R.id.appwidget_agentImage, null)
                views.setViewVisibility(R.id.appwidget_gameMode, View.GONE)
            }
        }
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)

}
