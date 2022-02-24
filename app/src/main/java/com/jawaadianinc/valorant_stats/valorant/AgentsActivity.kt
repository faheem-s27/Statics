package com.jawaadianinc.valorant_stats.valorant

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.jawaadianinc.valorant_stats.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element


class AgentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agents)

        val FullName = intent.extras!!.getString("Name")
        val nameSplit = FullName?.split("#")

        val Name = nameSplit?.get(0)
        val ID = nameSplit?.get(1)

        val topagentText : TextView = findViewById(R.id.topagentText)

        val URLAgents = "https://tracker.gg/valorant/profile/riot/${Name}%23$ID/agents?playlist=competitive&season=all"

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Fetching Agent data")
        progressDialog.setMessage("Please wait a moment")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) // There are 3 styles, You'll figure it out :)
        progressDialog.setCancelable(false)
        progressDialog.show()


        doAsync {
            try{
                val document: Document = Jsoup.connect(URLAgents).get()
                val FirstAgentName: Element? = document.select("#app > div.trn-wrapper > div.trn-container > div > main > div.content.no-card-margin > div.site-container.trn-grid.trn-grid--vertical.trn-grid--small > div.trn-grid.trn-grid--small > div > div > div:nth-child(2) > div > div.agent__agent > div.agent__name > span.agent__name-name").first()
                uiThread {
                    progressDialog.dismiss()
                    topagentText.text = "Top Agent:\n${FirstAgentName?.text()}\nMore Coming soon\nJUST A TEST!?!?!??!"
                }
            }
            catch (e: Exception){
                progressDialog.dismiss()
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@AgentsActivity).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }


    }
}
