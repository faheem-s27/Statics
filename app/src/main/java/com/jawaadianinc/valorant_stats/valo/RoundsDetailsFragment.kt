package com.jawaadianinc.valorant_stats.valo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject


class RoundsDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rounds_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressRoundsDetails)
        val timelineView: LinearLayout = view.findViewById(R.id.timeLineview)

        val teamWon = ArrayList<String>()
        val endingType = ArrayList<String>()
        val roundNumber = ArrayList<String>()

        doAsync {
            try {
                val jsonDetails = MatchHistoryActivity.matchJSON
                val matchData = jsonDetails?.get("data") as JSONObject
                val rounds = matchData.getJSONArray("rounds")
                uiThread {
                    val roundsListView: ListView = view.findViewById(R.id.RoundsList)
                    val roundsAdapter =
                        RoundAdapter(requireActivity(), teamWon, endingType, roundNumber)

                    for (i in 0 until rounds.length()) {
                        val roundDetails = rounds[i] as JSONObject
                        val winning_team = roundDetails.getString("winning_team")
                        val button = Button(activity?.applicationContext!!)
                        if (winning_team == "Blue") {
                            button.setBackgroundColor(Color.parseColor("#18e4b7"))
                        } else {
                            button.setBackgroundColor(Color.parseColor("#f94555"))
                        }
                        //button.text = (i + 1).toString()
                        button.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams(
                                25,
                                50
                            )
                        )
                        val transparentButton = Button(activity?.applicationContext!!)
                        transparentButton.alpha = 0F
                        transparentButton.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams(
                                10, 50
                            )
                        )

                        val midButton = Button(activity?.applicationContext!!)
                        midButton.alpha = 0F
                        midButton.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams(
                                40, 50
                            )
                        )

                        if (i == 12) {
                            timelineView.addView(midButton)
                        }
                        timelineView.addView(button)
                        timelineView.addView(transparentButton)
                        val ending = roundDetails.getString("end_type")
                        val number = i + 1
                        teamWon += winning_team
                        endingType += ending
                        roundNumber += number.toString()
                    }
                    progressBar.visibility = View.GONE
                    roundsListView.adapter = roundsAdapter
                }

            } catch (e:Exception){
                uiThread {
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }

    }

}
