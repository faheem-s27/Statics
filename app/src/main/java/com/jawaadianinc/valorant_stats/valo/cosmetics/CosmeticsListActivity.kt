package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.ValorantMainMenu
import com.jawaadianinc.valorant_stats.valo.cosmetics.weapon.WeaponActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL


class CosmeticsListActivity : AppCompatActivity() {
    private var cosmetic: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cosmetics_list)

        val title: TextView = findViewById(R.id.titleCosmetic)

        cosmetic = intent.getStringExtra("cosmetic")
        if (cosmetic == null) {
            startActivity(Intent(this, ValorantMainMenu::class.java))
        }

        if (cosmetic!!.lowercase() == "weapon") {
            title.text = "vAlorAnt weApons"
            getWeapons()
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

    companion object {
        var weaponJSON: JSONObject? = null
        var weaponIndex: Int? = null
    }
}
