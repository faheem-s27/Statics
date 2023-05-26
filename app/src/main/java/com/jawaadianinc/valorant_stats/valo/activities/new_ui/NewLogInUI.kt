package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityNewLogInUiBinding

class NewLogInUI : AppCompatActivity() {
    lateinit var binding : ActivityNewLogInUiBinding
    lateinit var webView : WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewLogInUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.riotSignInWebVIEW

        webView.settings.javaScriptEnabled = true
        val url = "https://auth.riotgames.com/authorize?client_id=statics&redirect_uri=https://statics-fd699.web.app/authorize.html&response_type=code&scope=openid+offline_access&prompt=login"
        webView.loadUrl(url)

        // hear the url change
        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // check if the url contains the code
                if (url.contains("code=")) {
                    // get the cookie from the url
                    val cookie = CookieManager.getInstance().getCookie(url)
                    // get the ssid from the cookie
                    val ssid = cookie.split(" ")[0].split("=")[1]
                    Toast.makeText(this@NewLogInUI, "Found Cookie: $ssid", Toast.LENGTH_SHORT).show()

                }
                return false
            }
        }
    }
}