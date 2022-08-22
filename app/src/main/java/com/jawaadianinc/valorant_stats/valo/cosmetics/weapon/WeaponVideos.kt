package com.jawaadianinc.valorant_stats.valo.cosmetics.weapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.WeaponVideoAdapter
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
import org.json.JSONObject

class WeaponVideos : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weapon_shop_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val weaponJSON: JSONObject? = CosmeticsListActivity.weaponJSON
        val index = CosmeticsListActivity.weaponIndex
        val names = ArrayList<String>()
        val videos = ArrayList<String>()

        val skins = weaponJSON!!.getJSONArray("data").getJSONObject(index!!).getJSONArray("skins")
        for (i in 0 until skins.length()) {
            val chromas = skins.getJSONObject(i).getJSONArray("chromas")
            for (j in 0 until chromas.length()) {
                try {
                    val chroma = chromas.getJSONObject(j)
                    val video = chroma.getString("streamedVideo")
                    if (video != "null") {
                        videos.add(video)
                        val name = chroma.getString("displayName")
                        names.add(name)
                        //Log.d("WeaponVideos", "Name: $name, Video: $video")
                    }
                } catch (e: Exception) {
                    // Do nothing for now (no video)
                }
            }
        }

        val adapter = WeaponVideoAdapter(requireActivity(), names, videos)
        val listView = view.findViewById<ListView>(R.id.weaponVideosListView)
        listView.adapter = adapter

        //Toast.makeText(context, "Got videos", Toast.LENGTH_SHORT).show()
    }


}
