package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
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
        //val url = "https://auth.riotgames.com/authorize?client_id=statics&redirect_uri=https://statics-fd699.web.app/authorize.html&response_type=code&scope=openid+offline_access&prompt=login"
        val url = "https://auth.riotgames.com/authorize?redirect_uri=https%3A%2F%2Fplayvalorant.com%2Fopt_in&client_id=play-valorant-web-prod&response_type=token%20id_token&nonce=1&prompt=login"
        // track cookies
        CookieManager.getInstance().setAcceptCookie(true)
        webView.loadUrl(url)

        // get the cookie for the specified domain
        val cookie: String = CookieManager.getInstance().getCookie(url) ?: ""
        // toast the access token
        Toast.makeText(this@NewLogInUI, "Cookie: $cookie", Toast.LENGTH_SHORT).show()

        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // check if the url contains the code
                if (url.contains("access_token=")) {
                    // get the cookie for the specified domain "auth.riotgames.com"
                    val cookieManager = CookieManager.getInstance()
                    val domain = "auth.riotgames.com" // Set the correct domain
                    val cookie = cookieManager.getCookie(domain)
                    Toast.makeText(this@NewLogInUI, "Cookie: $cookie", Toast.LENGTH_SHORT).show()
                }
                return false
            }
        }
    }
}