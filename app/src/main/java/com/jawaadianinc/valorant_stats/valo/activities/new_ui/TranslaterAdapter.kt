package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class TranslaterAdapter(private val people: List<Translater>) :
    RecyclerView.Adapter<TranslaterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.about_translater_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = people[position]
        holder.name.text = person.name
        Picasso.get().load(person.image).into(holder.image)
        holder.discord.text = person.discord
        holder.language.text = person.language
    }

    override fun getItemCount(): Int {
        return people.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.translater_image)
        val name: TextView = itemView.findViewById(R.id.translater_name)
        val discord : TextView = itemView.findViewById(R.id.translater_discord)
        val language : TextView = itemView.findViewById(R.id.translater_language)
    }
}
