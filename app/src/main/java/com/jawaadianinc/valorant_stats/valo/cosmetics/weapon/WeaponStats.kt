package com.jawaadianinc.valorant_stats.valo.cosmetics.weapon

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
import com.squareup.picasso.Picasso
import org.json.JSONObject

class WeaponStats : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weapon_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val json: JSONObject? = CosmeticsListActivity.weaponJSON
        val index = CosmeticsListActivity.weaponIndex

        val weaponImage = view.findViewById<ImageView>(R.id.imageWeapon)
        val url = json!!.getJSONArray("data").getJSONObject(index!!).getJSONObject("shopData")
            .getString("newImage")
        Picasso.get().load(url).into(weaponImage)
        val arrayList = ArrayList<String>()
        val mAdapter = object :
            ArrayAdapter<String?>(
                activity?.applicationContext!!, android.R.layout.simple_list_item_1,
                arrayList as List<String?>
            ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val item = super.getView(position, convertView, parent) as TextView
                item.setTextColor(Color.parseColor("#FFFFFF"))
                item.setTypeface(item.typeface, Typeface.BOLD)
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                return item
            }
        }
        try {
            val data = json.getJSONArray("data").getJSONObject(index)
            val weaponStatsJson =
                data.getJSONObject("weaponStats")

            val listView: ListView = view.findViewById(R.id.weaponStatsLV)


            listView.adapter = mAdapter
            mAdapter.add("------- General stats -------")
            mAdapter.add("Cost : " + (data?.getJSONObject("shopData")?.getString("cost") ?: "N/A"))
            mAdapter.add("Fire Rate: " + (weaponStatsJson.getString("fireRate") ?: "N/A"))
            mAdapter.add("Magazine Size : " + (weaponStatsJson.getString("magazineSize") ?: "N/A"))
            mAdapter.add(
                ("Reload Time : " + weaponStatsJson.getString("reloadTimeSeconds") + "s")
            )
            mAdapter.add(
                ("Equip Time : " + weaponStatsJson.getString("equipTimeSeconds") + "s")
            )
            mAdapter.add(
                "Run Speed Multiplier : " + (weaponStatsJson.getString("runSpeedMultiplier")
                    ?: "N/A")
            )
            mAdapter.add(
                "First Bullet Accuracy : " + (weaponStatsJson.getString("firstBulletAccuracy")
                    ?: "N/A")
            )
            mAdapter.add(
                "Shotgun Pellet Count : " + (weaponStatsJson.getString("shotgunPelletCount")
                    ?: "N/A")
            )
            mAdapter.add("------- ADS stats -------")
            try {
                mAdapter.add(
                    "Fire Rate: " + (weaponStatsJson.getJSONObject("adsStats")
                        ?.getString("fireRate")
                        ?: "N/A")
                )
                mAdapter.add(
                    "Zoom Multiplier : " + (weaponStatsJson.getJSONObject("adsStats")
                        ?.getString("zoomMultiplier") ?: "N/A")
                )
                mAdapter.add(
                    "Run Speed Multiplier : " + (weaponStatsJson.getJSONObject("adsStats")
                        ?.getString("runSpeedMultiplier") ?: "N/A")
                )
                mAdapter.add(
                    "First Bullet Accuracy : " + (weaponStatsJson.getJSONObject("adsStats")
                        ?.getString("firstBulletAccuracy") ?: "N/A")
                )
            } catch (e: Exception) {
                mAdapter.add("No ADS stats")
            }


            mAdapter.add("------- Damage Ranges -------")
            val damageRange = weaponStatsJson.getJSONArray("damageRanges")
            for (i in 0 until (damageRange?.length() ?: 0)) {
                mAdapter.add(
                    "--- For " + (damageRange?.getJSONObject(i)
                        ?.getString("rangeStartMeters")
                        ?: "N/A") + " - " + (damageRange?.getJSONObject(
                        i
                    )
                        ?.getString("rangeEndMeters") ?: "N/A") + " meters ---"
                )
                mAdapter.add(
                    "Headshot Damage : " + (damageRange?.getJSONObject(i)?.getString("headDamage")
                        ?: "N/A")
                )
                mAdapter.add(
                    "Body Damage : " + (damageRange?.getJSONObject(i)?.getString("bodyDamage")
                        ?: "N/A")
                )
                mAdapter.add(
                    "Leg Damage : " + (damageRange?.getJSONObject(i)?.getString("legDamage")
                        ?: "N/A")
                )
            }

        } catch (e: Exception) {
            mAdapter.add("No stats")
        }
    }

}
