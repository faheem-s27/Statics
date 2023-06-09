package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class CurrentLoadoutWeapon(private val weapons: List<LiveStatsFragment.Weapon>) :
    RecyclerView.Adapter<CurrentLoadoutWeapon.ViewHolder>() {

    private var weaponListener : OnWeaponClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.live_player_loadout_weapons, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val weapon = weapons[position]
        holder.weaponName.text = weapon.name
        Picasso.get().load(weapon.imageString).into(holder.weaponImage)

       holder.itemView.setOnClickListener {
           weaponListener?.onWeaponClick(weapon)
       }
    }

    override fun getItemCount(): Int {
        return weapons.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val weaponImage: ImageView = itemView.findViewById(R.id.weaponImage)
        val weaponName: TextView = itemView.findViewById(R.id.weaponName)
    }

    fun setOnWeaponClickListener(listener: OnWeaponClickListener) {
        weaponListener = listener
    }

    interface OnWeaponClickListener{
        fun onWeaponClick(weapon: LiveStatsFragment.Weapon)
    }
}
