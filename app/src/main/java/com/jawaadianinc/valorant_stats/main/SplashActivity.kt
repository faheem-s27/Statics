package com.jawaadianinc.valorant_stats.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.jawaadianinc.valorant_stats.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        // get locale from shared preferences
        val localPrefs = getSharedPreferences("UserLocale", MODE_PRIVATE)
        val localeString = localPrefs.getString("locale", "")
        if (localeString != "") {
            val localeArray = localeString!!.split("-")
            val localeLanguage = localeArray[0]
            val localeCountry = localeArray[1]
            val locale = java.util.Locale(localeLanguage, localeCountry)
            java.util.Locale.setDefault(locale)
            val config = android.content.res.Configuration()
            config.locale = locale
            baseContext.resources.updateConfiguration(
                config,
                baseContext.resources.displayMetrics
            )
            startActivity(Intent(this, LoadingActivity::class.java))
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            finish()
        } else {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            dialog.setTitle("Select Language")
            //dialog.setMessage("Please select your language. This will be used to display the app in your language.")
            // show a list of languages, English, French, Portuguese, Portuguese Brazil, Russian and Vietnamese
            val languages = arrayOf(
                "English",
                "French",
                "Indonesian",
                "Portuguese",
                "Portuguese (Brazil)",
                "Russian",
                "Turkish",
                "Vietnamese"
            )
            val languageCodes = arrayOf(
                "en-US",
                "fr-FR",
                "id-ID",
                "pt-PT",
                "pt-BR",
                "ru-RU",
                "tr-TR",
                "vi-VN"
            )

            dialog.setItems(languages) { _, which ->
                val language = languageCodes[which].split("-")[0]
                val country = languageCodes[which].split("-")[1]
                setLanguage(language, country)
            }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    private fun setLanguage(language: String, country: String) {
        val localPrefs = getSharedPreferences("UserLocale", MODE_PRIVATE)
        val locale = java.util.Locale(language, country)
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(
            config,
            baseContext.resources.displayMetrics
        )
        with(localPrefs.edit()) {
            putString("locale", "$language-$country")
            apply()
        }
        startActivity(Intent(this, LoadingActivity::class.java))
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        finish()
    }
}
