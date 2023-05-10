package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.ValorantMainMenu
import com.jawaadianinc.valorant_stats.valo.adapters.CosmeticAdapter
import com.jawaadianinc.valorant_stats.valo.cosmetics.weapon.WeaponActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.IOException
import java.net.URL


class CosmeticsListActivity : AppCompatActivity() {
    private var cosmetic: String? = null
    private lateinit var searchView: SearchView
    private var BASE_URL = "https://valorant-api.com/v1/"
    private var LANGUAGE = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cosmetics_list)
        searchView = findViewById(R.id.searchViewCosmetics)

        cosmetic = intent?.getStringExtra("cosmetic")
        if (cosmetic == null) {
            startActivity(Intent(this, ValorantMainMenu::class.java))
        }

        // get the language from shared preferences
        val sharedPref = getSharedPreferences("UserLocale", MODE_PRIVATE)
        LANGUAGE = sharedPref.getString("locale", "").toString()
        LANGUAGE = "?language=$LANGUAGE"

        //Toast.makeText(this, "Language: $LANGUAGE", Toast.LENGTH_SHORT).show()

        val toolbar = findViewById<Toolbar>(R.id.toolbar3)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.title = "Valorant $cosmetic"

        if (cosmetic?.lowercase() == "weapons") {
            getWeapons()
        }
        if (cosmetic == "cards") {
            getCards()
        }
        if (cosmetic == "sprays") {
            getSprays()
        }

        if (cosmetic == "buddies") {
            getBuddies()
        }

        if (cosmetic == "maps") {
            getMaps()
        }

        if (cosmetic == "ranks") {
            getRanks()
        }

        if (cosmetic == "borders") {
            getBorders()
        }

        val largeCard = findViewById<Button>(R.id.largeCardBT)
        val smallCard = findViewById<Button>(R.id.smallCardBT)
        val wideCard = findViewById<Button>(R.id.wideCardBT)

        val cardSize = intent?.getStringExtra("size")
        if (cardSize == null) {
            // hide the card size buttons
            largeCard.visibility = Button.INVISIBLE
            smallCard.visibility = Button.INVISIBLE
            wideCard.visibility = Button.INVISIBLE
        }

        largeCard.setOnClickListener {
            val size = intent.extras?.get("size")
            if (size == "large") {
                Toast.makeText(this, "Already selected", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", cosmetic)
                intent.putExtra("size", "large")
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                finish()
            }

        }
        smallCard.setOnClickListener {
            val size = intent.extras?.get("size")
            if (size == "small") {
                Toast.makeText(this, "Already selected", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", cosmetic)
                intent.putExtra("size", "small")
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                finish()
            }
        }

        wideCard.setOnClickListener {
            val size = intent.extras?.get("size")
            if (size == "wide") {
                Toast.makeText(this, "Already selected", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", cosmetic)
                intent.putExtra("size", "wide")
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                finish()
            }
        }
    }

    private fun getBorders() {
        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()

        val url = BASE_URL + "levelborders" + LANGUAGE
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Border",
                            "Download Border"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Border",
                            "Download Border"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }
        })

        doAsync {
            val result = URL(url).readText()
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val jsonInner = jsonArray.getJSONObject(i)
                names.add(jsonInner.get("startingLevel").toString() + " - Level")
                images.add(jsonInner.getString("levelNumberAppearance"))

                names.add(jsonInner.get("startingLevel").toString() + " - Player Card")
                images.add(jsonInner.getString("smallPlayerCardAppearance"))

            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = images[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Border",
                            "Download Border"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), names[position])
                            }
                        }
                    }
                    builder.show()
                }
            }
        }

    }

    private fun getRanks() {
        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()
        val url = BASE_URL + "competitivetiers" + LANGUAGE
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Rank icon",
                            "Download Rank icon"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Rank icon",
                            "Download Rank icon"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }
        })

        doAsync {
            val result = URL(url).readText()
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONArray("data")
            // get the last index of the array
            val lastIndex = jsonArray.length() - 1
            val ranksArray = jsonArray.getJSONObject(lastIndex).getJSONArray("tiers")
            for (i in 0 until ranksArray.length()) {
                val jsonInner = ranksArray.getJSONObject(i)
                if (jsonInner?.getString("largeIcon") != null) {
                    names.add(jsonInner.getString("tierName"))
                    images.add(jsonInner.getString("largeIcon"))

                    names.add(jsonInner.getString("tierName") + " (down)")
                    images.add(jsonInner.getString("rankTriangleDownIcon"))

                    names.add(jsonInner.getString("tierName") + " (up)")
                    images.add(jsonInner.getString("rankTriangleUpIcon"))
                }
            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = images[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Rank icon",
                            "Download Rank icon"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), names[position])
                            }
                        }
                    }
                    builder.show()
                }
            }
        }


    }

    private fun getMaps() {
        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()

        val url = BASE_URL + "maps" + LANGUAGE
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Map",
                            "Download Map"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Map",
                            "Download Map"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }
        })

        doAsync {
            val result = URL(url).readText()
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val jsonInner = jsonArray.getJSONObject(i)
                names.add(jsonInner.getString("displayName") + " - Minimap")
                images.add(jsonInner.getString("displayIcon"))

                names.add(jsonInner.getString("displayName") + " - List")
                images.add(jsonInner.getString("listViewIcon"))

                names.add(jsonInner.getString("displayName") + " - Full")
                images.add(jsonInner.getString("splash"))
            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = images[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Map",
                            "Download Map"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), names[position])
                            }
                        }
                    }
                    builder.show()
                }
            }
        }

    }

    private fun getBuddies() {
        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()

        val url = BASE_URL + "buddies" + LANGUAGE
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Buddy",
                            "Download Buddy"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Buddy",
                            "Download Buddy"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }
        })

        doAsync {
            val result = URL(url).readText()
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val jsonInner = jsonArray.getJSONObject(i)
                names.add(jsonInner.getString("displayName"))
                images.add(jsonInner.getString("displayIcon"))
            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = images[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Buddy",
                            "Download Buddy"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), names[position])
                            }
                        }
                    }
                    builder.show()
                }
            }
        }

    }

    private fun getSprays() {
        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Spray",
                            "Download Spray"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = filteredImages[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Spray",
                            "Download Spray"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }
        })

        val url =  BASE_URL + "sprays" + LANGUAGE
        doAsync {
            val result = URL(url).readText()
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val jsonInner = jsonArray.getJSONObject(i)
                names.add(jsonInner.getString("displayName"))
                images.add(jsonInner.getString("fullTransparentIcon"))
            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    // when clicked
                    val imageURL = images[position]
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Spray",
                            "Download Spray"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), names[position])
                            }
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    private fun getWeapons() {
        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val name = filteredNames[position]
                    // make weaponIndex the index of the weapon in the list
                    weaponIndex = names.indexOf(name)
                    gotoWeaponStats(name)
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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val name = filteredNames[position]
                    weaponIndex = names.indexOf(name)
                    gotoWeaponStats(name)
                }
                return false
            }
        })

        val url = BASE_URL + "weapons" + LANGUAGE
        doAsync {
            val result = JSONObject(URL(url).readText())
            val weapons = result.getJSONArray("data")
            weaponJSON = result
            for (i in 0 until weapons.length()) {
                val weapon = weapons.getJSONObject(i)
                val name = weapon.getString("displayName")
                val image = weapon.getString("displayIcon")
                names.add(name)
                images.add(image)
            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val name = names[position]
                    weaponIndex = position
                    gotoWeaponStats(name)
                }
            }
        }
    }

    private fun gotoWeaponStats(weapon: String) {
        val intent = Intent(this, WeaponActivity::class.java)
        intent.putExtra("weaponName", weapon)
        startActivity(intent)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    private fun getCards() {
        val url = BASE_URL + "playercards" + LANGUAGE
        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val imageURL = filteredImages[position]
                    // show alert dialog with options being "show image" and "download image" and "set wallpaper"
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Card",
                            "Download Card",
                            "Set card as wallpaper"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
                            }
                            2 -> {
                                setWallpaper(imageURL)
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

                val filteredAdapter = CosmeticAdapter(
                    this@CosmeticsListActivity,
                    "weapon",
                    filteredNames,
                    filteredImages
                )
                filteredAdapter.notifyDataSetChanged()
                listView.adapter = filteredAdapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val imageURL = filteredImages[position]
                    // show alert dialog with options being "show image" and "download image" and "set wallpaper"
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Card",
                            "Download Card",
                            "Set card as wallpaper"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                // download image from url
                                mSaveMediaToStorage(getBitMap(imageURL), filteredNames[position])
                            }
                            2 -> {
                                setWallpaper(imageURL)
                            }
                        }
                    }
                    builder.show()
                }
                return false
            }
        })

        val size = intent.extras?.get("size")

        doAsync {
            val json = JSONObject(URL(url).readText()).getJSONArray("data")
            for (i in 0 until json.length()) {
                val card = json.getJSONObject(i)
                val name = card.getString("displayName")
                val image = card.getString("${size}Art")
                names.add(name)
                images.add(image)
            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { _, _, position, _ ->
                    val imageURL = images[position]
                    // show alert dialog with options being "show image" and "download image" and "set wallpaper"
                    val builder = AlertDialog.Builder(this@CosmeticsListActivity)
                    builder.setTitle("Choose an option")
                    builder.setItems(
                        arrayOf(
                            "View Card",
                            "Download Card",
                            "Set card as wallpaper"
                        )
                    ) { _, which ->
                        when (which) {
                            0 -> {
                                showPhotoURL(imageURL)
                            }
                            1 -> {
                                mSaveMediaToStorage(getBitMap(imageURL), names[position])
                            }
                            2 -> {
                                setWallpaper(imageURL)
                            }
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    companion object {
        var weaponJSON: JSONObject? = null
        var weaponIndex: Int? = null
    }

    private fun showPhotoURL(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "image/*")
        this.startActivity(intent)
    }

    private fun getBitMap(url: String): String {
        return url
    }

    private fun setWallpaper(url: String) {
        Picasso.get().load(url).transform(BlurTransformation(this, 1))
            .into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    val wallpaperManager =
                        WallpaperManager.getInstance(this@CosmeticsListActivity)
                    try {
                        wallpaperManager.setBitmap(bitmap)
                        Toast.makeText(
                            this@CosmeticsListActivity,
                            "Wallpaper set successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    Toast.makeText(
                        this@CosmeticsListActivity,
                        "Error setting wallpaper",
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

    private fun mSaveMediaToStorage(url: String, photoName: String) {
        Picasso.get().load(url).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                val mediaStore = MediaStore.Images.Media.insertImage(
                    contentResolver,
                    bitmap,
                    photoName,
                    "Downloaded from Statics for Valorant"
                )
                mediaStore?.let {
                    Toast.makeText(
                        this@CosmeticsListActivity,
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
