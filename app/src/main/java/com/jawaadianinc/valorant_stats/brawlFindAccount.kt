package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class brawlFindAccount : AppCompatActivity() {
    var ENDPOINT = ""
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var imagebackground: ImageView
    val imagesURL = java.util.ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brawl_find_account)
        val brawlID: EditText = findViewById(R.id.brawlID)
        val search: Button = findViewById(R.id.brawlStats)

        imagebackground = findViewById(R.id.brawlBackground)
        imagesURL.add("https://www.brawlhalla.com/c/uploads/2020/12/SunThroughTrees_1920x1080.jpg")
        imagesURL.add("https://ucafdd70d55cdd9a5b66d1bc5ea5.previews.dropboxusercontent.com/p/thumb/ABY_-DOsrDG1eueo81EC3aWn5OyF0hOZBBqKJgBj5fDD6TPXvAjF8qOW_vlTCWXpgKv9YW3qktuRjMsQ8On_sJfS30B_l8qQt_LLKw8wd5WA_608tieqNMpVQWu8X0MZGfl3uU8mu6cQfQiJKzqwbJAphSla5tVwkjs6f6QQLJelSnU5WCfCS1gKIi_82lI8FrWRcHAceeh9LZA7VmZNW2rWFUmhsgnI6J5bWSKGY4dXTen075Mx5_dB5rvLoO5U-ipnWks_2kXF7DSUrxUoL_lSh3gVXQnARZET_BSx-C-4O7s5qO5M9R9GidCy8HnYrZsmLDxy6fL1ouyosArUXJ57q71aqKkTvzyTgQm8ZPEDokcYG2kLldBIAZVERNWX-ZM/p.jpeg")
        imagesURL.add("https://www.brawlhalla.com/c/uploads/2020/09/KeyArt_EpicNix_1920x1080_WithLogo2.jpg")
        imagesURL.add("https://www.brawlhalla.com/c/uploads/2019/04/FaitSelfie_1920x1080.jpg")
        imagesURL.add("https://www.brawlhalla.com/c/uploads/2019/06/PetraClassic_1920x1080.jpg")
        imagesURL.add("https://uc54cf2170ad3ca6d4601113c247.previews.dropboxusercontent.com/p/thumb/ABbMsyZJejaSfiBWmniRAyKpDFtr4qAQJpUv7_wEywX8HkPW6yJveb5exZIoqnep3_QRYD-adOXAennrRifaerqU3ixXiRsU7dixpwisxZ_Npm8W7P5wuChKL1y80JbidkQaCYj3jZlFosfspFT90ckJ1qxVmNENFJO_V6k8JreWHAWFeWXFuqG5IEYDZTRHm49EiEphWIoOZGjbFzgVeKq2Iea29eqkLVJ1I_siFxdGaPadjKvOTNMV6NHSzpNylf5f5isOmsMyZxwpfsA_tAbdbeYey7SebEMznYAN7Da6E5YjIAWxFuzwCgULvoeNKasFDfDyYXSZWJTD4VwOqlST_1npq91uZds52MZxooj0Hby6berRCTGdxCpyLp2ZMBg/p.jpeg")

        Picasso.get().load(imagesURL.random()).into(imagebackground)

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            doTask(handler)
        }
        handler.postDelayed(runnable, 2000)

        search.setOnClickListener {
            if (brawlID.text.toString().isNotEmpty()) {
                val intent = Intent(this, brawlStatsActivity::class.java)
                intent.putExtra("BrawlID", brawlID.text.toString())
                startActivity(intent)
            } else {
                val contextView = findViewById<View>(R.id.brawlID)
                val snackbar = Snackbar
                    .make(contextView, "User is empty!", Snackbar.LENGTH_LONG)
                snackbar.show()
            }

        }
    }

    companion object {
        const val URL = "https://api.brawlhalla.com"
    }

    private fun doTask(handler: Handler) {
        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable)
            .into(imagebackground)
        handler.postDelayed(runnable, 5000)
    }
}
