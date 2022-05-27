package com.jawaadianinc.valorant_stats.valo.cosmetics.weapon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
import com.jawaadianinc.valorant_stats.valo.cosmetics.WeaponVideoAdapter
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

        val adapter = WeaponVideoAdapter(requireActivity(), "videos", names, videos)
        val listView = view.findViewById<ListView>(R.id.weaponVideosListView)
        listView.adapter = adapter

        //Toast.makeText(context, "Got videos", Toast.LENGTH_SHORT).show()
    }


    private fun setupVideo(name: String, video: String) {


//        val layOut: ConstraintLayout = requireView().findViewById(R.id.frameLayout7)
//        val videoView = VideoView(context)
//        videoView.setVideoURI(Uri.parse(video))
//        videoView.setOnPreparedListener {
//            //videoView.start()
//        }
//
//        // make video contraint to match parent on width and wrap content on height
//        val params = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.MATCH_PARENT,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT
//        )
//        videoView.layoutParams = params

        // add video below the previous one
//        try {
//            val previousVideo = layOut.getChildAt(layOut.childCount - 1)
//            layOut.addView(videoView, layOut.indexOfChild(previousVideo) + 1)
//        } catch (e: Exception) {
//            layOut.addView(videoView)
//        }
        //layOut.addView(videoView)
    }
}
