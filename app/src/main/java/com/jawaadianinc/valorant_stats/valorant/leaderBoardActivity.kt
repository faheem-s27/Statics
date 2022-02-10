package com.jawaadianinc.valorant_stats.valorant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jawaadianinc.valorant_stats.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class leaderBoardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leader_board)

        val search: EditText = findViewById(R.id.searchLeaderboard)
        val progress: ProgressBar = findViewById(R.id.progressBar)
        val leaderboardList: ListView = findViewById(R.id.leaderList)
        val loadingText: TextView = findViewById(R.id.loadingText)
        val arrayList = ArrayList<String>()
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
        leaderboardList.adapter = mAdapter
        leaderboardList.visibility = View.INVISIBLE
        search.visibility = View.INVISIBLE
        var totalPlayers = 0

        doAsync {
            try {
                val json =
                    JSONObject(URL("https://api.henrikdev.xyz/valorant/v2/leaderboard/eu").readText())
                val players = json["players"] as JSONArray
                uiThread {
                    progress.max = players.length()
                    for (i in 0 until players.length()) {
                        val currentPlayer = players[i] as JSONObject
                        val name = currentPlayer["gameName"] as String
                        val tag = currentPlayer["tagLine"] as String
                        if (name != "") {
                            mAdapter.add("${name}#${tag}")
                            totalPlayers += 1
                        }
                        progress.progress = i
                    }
                }
                uiThread {
                    progress.visibility = View.INVISIBLE
                    leaderboardList.visibility = View.VISIBLE
                    loadingText.visibility = View.INVISIBLE
                    search.visibility = View.VISIBLE
                    val contextView = findViewById<View>(R.id.loadingText)
                    val snackbar = Snackbar
                        .make(contextView, "Loaded $totalPlayers players", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            } catch (e: Exception) {
                uiThread {
                    AlertDialog.Builder(this@leaderBoardActivity).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }

        leaderboardList.setOnItemClickListener { _: AdapterView<*>, _: View, i: Int, _: Long ->
            copyText(leaderboardList.getItemAtPosition(i) as String)
            Toast.makeText(this, "Copied Name!", Toast.LENGTH_SHORT).show()
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                mAdapter.filter.filter(s);
            }
        })
    }
    private fun copyText(text: String) {
        val myClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
    }

}
