package com.jawaadianinc.valorant_stats.valo.match_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R


class RoundOverview : Fragment() {
    private var xMult: Double = 0.0
    private var yMult: Double = 0.0
    private var xScalar: Double = 0.0
    private var yScalar: Double = 0.0
    private var mapofPlayerandAgent: MutableMap<String, String> = mutableMapOf("player" to "agent")

    private val jsonDetails = MatchHistoryActivity.matchJSON

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_round_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }


    private fun TextView.setTextAnimation(
        text: String,
        duration: Long = 300,
        completion: (() -> Unit)? = null
    ) {
        fadOutAnimation(duration) {
            this.text = text
            fadInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }

    private fun View.fadOutAnimation(
        duration: Long = 300,
        visibility: Int = View.INVISIBLE,
        completion: (() -> Unit)? = null
    ) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                this.visibility = visibility
                completion?.let {
                    it()
                }
            }
    }

    private fun View.fadInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                completion?.let {
                    it()
                }
            }
    }

}
