package com.jawaadianinc.valorant_stats.valo.cosmetics.weapon

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.CosmeticAdapter
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
import com.squareup.picasso.Picasso
import org.json.JSONObject

class WeaponSkins : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weapon_skins, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val json: JSONObject? = CosmeticsListActivity.weaponJSON
        val index = CosmeticsListActivity.weaponIndex
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = view.findViewById(R.id.weaponSkinsList)

        val searchView = view.findViewById(R.id.searchViewWeaponSkins) as SearchView

        val skins = json!!.getJSONArray("data").getJSONObject(index!!).getJSONArray("skins")
        for (i in 0 until skins.length()) {
            val chromas = skins.getJSONObject(i).getJSONArray("chromas")
            for (j in 0 until chromas.length()) {
                names.add(chromas.getJSONObject(j).getString("displayName"))
                images.add(chromas.getJSONObject(j).getString("fullRender"))
            }
        }

        // listen to search view on text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filteredImages = ArrayList<String>()
                val filteredNames = ArrayList<String>()
                for (name in names) {
                    if (name.contains(query.toString(), true)) {
                        filteredNames.add(name)
                        filteredImages.add(images[names.indexOf(name)])
                    }
                }

                val filteredAdapter =
                    CosmeticAdapter(requireActivity(), "skins", filteredNames, filteredImages)
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Skin",
                            "Download Skin"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(imageURL, filteredNames[position])
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // make a new name array that contains the newText and set the adapter to that and filtered images
                val filteredImages = ArrayList<String>()
                val filteredNames = ArrayList<String>()
                for (name in names) {
                    if (name.contains(newText.toString(), true)) {
                        filteredNames.add(name)
                        filteredImages.add(images[names.indexOf(name)])
                    }
                }

                Log.d("filteredNames", filteredNames.toString())

                val filteredAdapter =
                    CosmeticAdapter(requireActivity(), "skins", filteredNames, filteredImages)
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Skin",
                            "Download Skin"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(imageURL, filteredNames[position])
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }
        })

        val adapter = CosmeticAdapter(requireContext() as Activity, "skins", names, images)
        listView.adapter = adapter
        // get item clicked
        listView.setOnItemClickListener { _, _, position, _ ->
            // when clicked
            val imageURL = images[position]
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose an option")
            builder.setItems(
                arrayOf(
                    "View Skin",
                    "Download Skin"
                )
            ) { _, which ->
                when (which) {
                    0 -> {
                        showPhotoURL(imageURL)
                    }
                    1 -> {
                        // download image from url
                        mSaveMediaToStorage(imageURL, names[position])
                    }
                }
            }
            builder.show()
        }
    }

    private fun showPhotoURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "image/*")
        context?.startActivity(intent)
    }

    private fun mSaveMediaToStorage(url: String, photoName: String) {
        Picasso.get().load(url).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                val mediaStore = MediaStore.Images.Media.insertImage(
                    requireActivity().contentResolver,
                    bitmap,
                    photoName,
                    "Downloaded from Statics for Valorant"
                )
                mediaStore?.let {
                    Toast.makeText(
                        requireContext(),
                        "Image downloaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }
        })
    }
}
