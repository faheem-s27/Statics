package com.jawaadianinc.valorant_stats.valo

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R

class MMRAdapter(
    private val context: Activity,
    private val date: ArrayList<String>,
    private val changeMMR: ArrayList<String>,
    private val currentNumber: ArrayList<String>,
    private val rankNames: ArrayList<String>
) : ArrayAdapter<Any?>(
    context, R.layout.mmr_layout, date as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.mmr_layout, null, true)
        val dateText = row!!.findViewById<View>(R.id.date) as TextView
        val changeText = row.findViewById<View>(R.id.changeMMR) as TextView
        val number = row.findViewById<View>(R.id.progressMMR) as ProgressBar
        val rank = row.findViewById<View>(R.id.rankText) as TextView
        val rankNumber = row.findViewById<View>(R.id.rankNumberMMRLayout) as TextView
        dateText.text = "On " + date[position]
        changeText.text = "Rank Change: " + changeMMR[position]
        if (changeMMR[position].toInt() <= 0) {
            changeText.setTextColor(Color.parseColor("#f94555"))
            number.progressTintList = ColorStateList.valueOf(Color.parseColor("#f94555"))
        } else {
            changeText.setTextColor(Color.parseColor("#18e4b7"))
            number.progressTintList = ColorStateList.valueOf(Color.parseColor("#18e4b7"))
        }

        return if (currentNumber[position].toInt() <= 100) {
            rank.text = rankNames[position]
            rankNumber.text = currentNumber[position] + "/100"
            number.progress = currentNumber[position].toInt()
            number.max = 100
            row
        } else {
            rank.text = rankNames[position]
            rankNumber.text = currentNumber[position]
            row
        }
    }
}
