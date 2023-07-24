package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.MySliderImageAdapter
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso

class MatchesAnalyserAdapter(private val stats: List<MatchAnalyser>) :
    RecyclerView.Adapter<MatchesAnalyserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_matches_loaded_data_analysing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = stats[position]
        val names = data.assetName

        val type = data.type
        val sortedList = data.sortedList

        val images = mutableListOf<String>()
        val number = mutableListOf<Int>()

        for (item in sortedList)
        {
            number+=item.second
            if (type == "Agent")
            {
                images+="https://media.valorant-api.com/agents/${item.first}/fullportrait.png"
            }
            if (type == "Map")
            {
                images+="https://media.valorant-api.com/maps/${item.first}/splash.png"
            }
        }

        val adapter = MySliderImageAdapter()
        adapter.renewItems(images as ArrayList<String>)
        holder.imageSlider.setSliderAdapter(adapter)

        holder.nameText.text = "${names[0]} was played ${number[0]} times"

        holder.imageSlider.setCurrentPageListener {
            holder.nameText.text = "${names[it]} was played ${number[it]} times"
        }
    }

    override fun getItemCount(): Int {
        return stats.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageSlider: SliderView = itemView.findViewById(R.id.imageSlider)
        val nameText: TextView = itemView.findViewById(R.id.objectName)
    }
}
