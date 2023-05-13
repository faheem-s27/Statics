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
                "Portuguese",
                "Portuguese (Brazil)",
                "Russian",
                "Vietnamese"
            )
            dialog.setItems(languages) { _, which ->
                when (which) {
                    0 -> {
                        val locale = java.util.Locale("en", "US")
                        with(localPrefs.edit()) {
                            putString("locale", "en-US")
                            apply()
                        }
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
                    }

                    1 -> {
                        val locale = java.util.Locale("fr", "FR")
                        java.util.Locale.setDefault(locale)
                        val config = android.content.res.Configuration()
                        config.locale = locale
                        baseContext.resources.updateConfiguration(
                            config,
                            baseContext.resources.displayMetrics
                        )
                        with(localPrefs.edit()) {
                            putString("locale", "fr-FR")
                            apply()
                        }
                        startActivity(Intent(this, LoadingActivity::class.java))
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                        finish()
                    }

                    2 -> {
                        val locale = java.util.Locale("pt", "PT")
                        java.util.Locale.setDefault(locale)
                        val config = android.content.res.Configuration()
                        config.locale = locale
                        baseContext.resources.updateConfiguration(
                            config,
                            baseContext.resources.displayMetrics
                        )
                        with(localPrefs.edit()) {
                            putString("locale", "pt-PT")
                            apply()
                        }
                        startActivity(Intent(this, LoadingActivity::class.java))
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                        finish()
                    }

                    3 -> {
                        val locale = java.util.Locale("pt", "BR")
                        java.util.Locale.setDefault(locale)
                        val config = android.content.res.Configuration()
                        config.locale = locale
                        baseContext.resources.updateConfiguration(
                            config,
                            baseContext.resources.displayMetrics
                        )
                        with(localPrefs.edit()) {
                            putString("locale", "pt-BR")
                            apply()
                        }
                        startActivity(Intent(this, LoadingActivity::class.java))
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                        finish()
                    }

                    4 -> {
                        val locale = java.util.Locale("ru", "RU")
                        java.util.Locale.setDefault(locale)
                        val config = android.content.res.Configuration()
                        config.locale = locale
                        baseContext.resources.updateConfiguration(
                            config,
                            baseContext.resources.displayMetrics
                        )
                        with(localPrefs.edit()) {
                            putString("locale", "ru-RU")
                            apply()
                        }
                        startActivity(Intent(this, LoadingActivity::class.java))
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                        finish()
                    }

                    5 -> {
                        val locale = java.util.Locale("vi", "VN")
                        java.util.Locale.setDefault(locale)
                        val config = android.content.res.Configuration()
                        config.locale = locale
                        baseContext.resources.updateConfiguration(
                            config,
                            baseContext.resources.displayMetrics
                        )
                        with(localPrefs.edit()) {
                            putString("locale", "vi-VN")
                            apply()
                        }
                        startActivity(Intent(this, LoadingActivity::class.java))
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                        finish()
                    }
                }
            }
            dialog.setCancelable(false)
            dialog.show()
        }


    }
}
