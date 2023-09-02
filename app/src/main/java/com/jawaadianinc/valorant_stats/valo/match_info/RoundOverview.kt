package com.jawaadianinc.valorant_stats.valo.match_info

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import kotlin.math.round


class RoundOverview : Fragment() {
    private val jsonDetails = MatchHistoryActivity.matchJSON

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_round_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val henrikMatch = Gson().fromJson(jsonDetails.toString(), HenrikMatch::class.java)

        val Name = requireActivity().intent.extras!!.getString("RiotName")
        val ID = requireActivity().intent.extras!!.getString("RiotID")

        val fullName = "$Name#$ID"

        for (player in henrikMatch.data.players.all_players) {
            if (player.name == Name && player.tag == ID) {
                loadPlayerCards(player)
                calculateShotsPercentages(player)
            }
        }

    }

    private fun loadPlayerCards(player: AllPlayer) {
        val nameTV = requireActivity().findViewById<TextView>(R.id.textView19)
        nameTV.text = "${player.name}#${player.tag}"
        val playerImage = requireActivity().findViewById<ImageView>(R.id.overview_player_image)
        Picasso.get()
            .load("https://media.valorant-api.com/playercards/${player.player_card}/displayicon.png")
            .into(playerImage)
        val agentImage = requireActivity().findViewById<ImageView>(R.id.overview_agent_image)
        Picasso.get().load(player.assets.agent.small).fit().centerInside()
            .into(agentImage)
    }


    private fun calculateShotsPercentages(player: AllPlayer) {
        val totalShots: Int
        val headShotsHits: Int
        val bodyShotsHits: Int
        val legsShotsHits: Int

        headShotsHits = player.stats.headshots
        bodyShotsHits = player.stats.bodyshots
        legsShotsHits = player.stats.legshots

        totalShots = headShotsHits + bodyShotsHits + legsShotsHits


        val HeadPercent: Float = (headShotsHits.toFloat() / totalShots) * 100
        val BodyPercent: Float = (bodyShotsHits.toFloat() / totalShots).toFloat() * 100
        val LegsPercent: Float = (legsShotsHits.toFloat() / totalShots).toFloat() * 100

        Log.d("HenrikMatch", "Headshots: $HeadPercent, Body: $BodyPercent, Legs: $LegsPercent")

        val headText = requireActivity().findViewById<TextView>(R.id.overview_headshots)
        headText.text = "${HeadPercent.round(1)}% $headShotsHits hits"
        val bodyText = requireActivity().findViewById<TextView>(R.id.overview_bodyshots)
        bodyText.text = "${BodyPercent.round(1)}% $bodyShotsHits hits"
        val legsText = requireActivity().findViewById<TextView>(R.id.overview_legshots)
        legsText.text = "${LegsPercent.round(1)}% $legsShotsHits hits"

        val headImage = requireActivity().findViewById<ImageView>(R.id.imageView12)
        tintImageView(headImage, (HeadPercent / 100).toFloat())
        val bodyImage = requireActivity().findViewById<ImageView>(R.id.imageView13)
        tintImageView(bodyImage, (BodyPercent / 100).toFloat())
        val legsImage = requireActivity().findViewById<ImageView>(R.id.imageView14)
        tintImageView(legsImage, (LegsPercent / 100).toFloat())

    }

    fun Float.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    private fun tintImageView(imageView: ImageView, tintPercentage: Float) {
        val colorToTint = Color.parseColor("#18e4b7")
        val alpha = (Color.alpha(colorToTint) * tintPercentage).toInt()
        val red = (Color.red(colorToTint) * tintPercentage).toInt()
        val green = (Color.green(colorToTint) * tintPercentage).toInt()
        val blue = (Color.blue(colorToTint) * tintPercentage).toInt()

        val tintedColor = Color.argb(alpha, red, green, blue)
        imageView.setColorFilter(tintedColor, PorterDuff.Mode.SRC_ATOP)
    }


}
