package com.jawaadianinc.valorant_stats.main

import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.jawaadianinc.valorant_stats.R
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element


class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lenny = """¯\_(ツ)_/¯"""

        val pInfo: PackageInfo =
            this.packageManager.getPackageInfo(this.packageName, 0)
        val version = pInfo.versionName

        val typeFace = ResourcesCompat.getFont(this, R.font.lalezar)

        val versionElement = Element()
        versionElement.title = "Version $version"

        val discordElement = Element()
        discordElement.title = "Join the Statics server"
        // get the int value of the drawable
        val discordIcon = R.drawable.icons8_discord_48
        discordElement.iconDrawable = discordIcon
        discordElement.iconTint = R.color.Discord
        val discord = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/hgacc2kVMa"))
        discordElement.intent = discord

        val aboutPage = AboutPage(this, true)
            .isRTL(false)
            .setCustomFont(typeFace)
            .setImage(R.drawable.just_statics_alot_smaller)
            .setDescription(
                "Hi! I'm the developer of Statics\n " +
                        "A Valorant Stats Tracker app, for people who want to see their Valorant stats on mobile more conveniently without using an external browser or PC\n" +
                        lenny + "\n" + "Mostly a hobby project, but I'm always open to suggestions and feedback\nLots of love ❤️, Faheem Saleem"
            )
            .addItem(versionElement)
            .addGroup("Support")
            .addItem(discordElement)
            .addEmail("staticsdeveloper@gmail.com")
            .addPlayStore("com.jawaadianinc.valorant_stats")
            .addInstagram("staticsdeveloper")
            .addWebsite("https://statics-fd699.web.app/")
            .addYoutube("UCpE_f5s4MHx1gO4S08scUQw")
            .addTwitter("StaticsDev")
            .create()

        setContentView(aboutPage)
    }
}
