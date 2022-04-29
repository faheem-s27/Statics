package com.jawaadianinc.valorant_stats.valo

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityValorantUpdatesBinding
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class ValorantUpdatesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityValorantUpdatesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityValorantUpdatesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = "Latest Updates!"
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Made By Faheem Saleem", Snackbar.LENGTH_LONG).show()
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Fetching Updates")
        progressDialog.setMessage("Please wait a moment")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) // There are 3 styles, You'll figure it out :)
        progressDialog.setCancelable(false)
        progressDialog.show()


        val arrayList = ArrayList<String>()
        val listview : ListView = findViewById(R.id.updateListView)
        listview.isNestedScrollingEnabled = true
        val mAdapter = object :
            ArrayAdapter<String?>(
                applicationContext!!, android.R.layout.simple_list_item_1,
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

        listview.adapter = mAdapter


        val requestURL = "https://api.henrikdev.xyz/valorant/v1/website/en-us"

        doAsync{
            try{
                val URLdata = URL(requestURL).readText()
                val json = JSONObject(URLdata)
                val data = json["data"] as JSONArray


                uiThread {
                    for (i in 0 until data.length()) {
                        val stuff = data[i] as JSONObject
                        mAdapter.add(stuff.getString("title"))

                    }
                }

                progressDialog.dismiss()



                uiThread {
                    listview.setOnItemClickListener { _, _, position, _ ->
                        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView: View = inflater.inflate(R.layout.showupdates, null)
                        val width = LinearLayout.LayoutParams.MATCH_PARENT
                        val height = LinearLayout.LayoutParams.MATCH_PARENT
                        val focusable = true
                        val popupWindow = PopupWindow(popupView, width, height, focusable)
                        popupWindow.showAtLocation(View(this@ValorantUpdatesActivity), Gravity.CENTER, 0, 0)

                        val dismissButton = popupView.findViewById(R.id.dismiss) as Button
                        dismissButton.setOnClickListener {
                            popupWindow.dismiss()
                        }
                        val data = data[position] as JSONObject
                        val URL = data.getString("url")
                        val webpage = popupView.findViewById(R.id.updatePage) as WebView
                        webpage.settings.javaScriptEnabled = true
                        webpage.loadUrl(URL)
                    }
                }


            }
            catch (e: Exception){
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@ValorantUpdatesActivity).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }

            }
        }
    }
}

