package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.ValorantMainMenu
import com.jawaadianinc.valorant_stats.valo.adapters.CosmeticAdapter
import com.jawaadianinc.valorant_stats.valo.cosmetics.weapon.WeaponActivity
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL


class CosmeticsListActivity : AppCompatActivity() {
    private var cosmetic: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cosmetics_list)

        val title: TextView = findViewById(R.id.titleCosmetic)

        cosmetic = intent?.getStringExtra("cosmetic")
        if (cosmetic == null) {
            startActivity(Intent(this, ValorantMainMenu::class.java))
        }

        if (cosmetic?.lowercase() == "weapon") {
            title.text = "vAlorAnt weApons"
            getWeapons()
        }
        if (cosmetic == "cards") {
            title.text = "vAlorAnt cards"
            getCards()
        }
        if (cosmetic == "sprays") {
            title.text = "vAlorAnt sprays"
            getSprays()
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

    private fun getSprays() {
        val progressDialog = ProgressDialog(this).apply {
            setTitle("Loading")
            setMessage("Collecting Data...")
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(false)
            show()
        }
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

        val URL = "https://valorant-api.com/v1/sprays"
        doAsync {
            val result = URL(URL).readText()
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val jsonInner = jsonArray.getJSONObject(i)
                names.add(jsonInner.getString("displayName"))
                images.add(jsonInner.getString("fullTransparentIcon"))

//                if (jsonInner.getString("animationPng") != "null") {
//                    images.add(jsonInner.getString("animationGif"))
//                    names.add(jsonInner.getString("displayName") + " (animation)")
//                }

            }
            uiThread {
                progressDialog.dismiss()
                val adapter = CosmeticAdapter(this@CosmeticsListActivity, "weapon", names, images)
                listView.adapter = adapter
                listView.setOnItemClickListener { parent, view, position, id ->
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
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Collecting Data...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

        val URL = "https://valorant-api.com/v1/weapons"
        doAsync {
            val result = JSONObject(URL(URL).readText())
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
        val url = "https://valorant-api.com/v1/playercards"
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Collecting Data...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()
        val names = ArrayList<String>()
        val images = ArrayList<String>()
        val listView: ListView = findViewById(R.id.cosmeticListView)

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
                                // download image from url
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

    private fun getBitMap(url: String): Bitmap {
        var bitmap: Bitmap? = null
        while (bitmap == null) {
            doAsync {
                bitmap = Picasso.get().load(url).get()
            }
        }
        return bitmap!!
    }

    private fun setWallpaper(url: String) {
        val bitmap = getBitMap(url)
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

    private fun mSaveMediaToStorage(bitmap: Bitmap?, photoName: String) {
        // check if photoname is a gif or png or jpg
        val extension = if (photoName.contains(".gif")) {
            "gif"
        } else if (photoName.contains(".png")) {
            "png"
        } else {
            "jpg"
        }

        doAsync {
            val filename = "$photoName - ${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CosmeticsListActivity().contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            fos?.use {
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, it)
                uiThread {
                    Toast.makeText(
                        this@CosmeticsListActivity,
                        "Saved to Gallery",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
