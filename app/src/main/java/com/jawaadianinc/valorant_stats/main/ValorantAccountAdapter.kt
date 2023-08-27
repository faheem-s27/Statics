package com.jawaadianinc.valorant_stats.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class ValorantAccountAdapter(
    private val accounts: List<ValorantAccount>,
    private val itemClickListener: OnItemClickListener,
    private val longClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<ValorantAccountAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.valorant_account_select_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]
        holder.name.text = account.name
        Picasso.get().load(account.getImage()).into(holder.image)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener {
        val image: ImageView = itemView.findViewById(R.id.translater_image)
        val name: TextView = itemView.findViewById(R.id.translater_name)

        init {
            itemView.setOnLongClickListener(this)

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val account = accounts[position]
                    itemClickListener.onItemClick(account)
                }
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val account = accounts[position]
                longClickListener.onItemLongClick(account, position)
                return true
            }
            return false
        }
    }

    interface OnItemClickListener {
        fun onItemClick(account: ValorantAccount)
        fun onItemLongClick(account: ValorantAccount, position: Int)
    }
}
