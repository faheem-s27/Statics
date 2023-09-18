package com.jawaadianinc.valorant_stats.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.NewLogInUI

data class ValorantAccount(
    val name: String,
    val imageID: String,
    val cookies: String,
    val puuid: String
) {
    fun getImage(): String {
        return "https://media.valorant-api.com/playercards/$imageID/smallart.png"
    }
}

class AccountSelectionActivity : AppCompatActivity(), ValorantAccountAdapter.OnItemClickListener,
    View.OnCreateContextMenuListener,
    PopupMenu.OnMenuItemClickListener {
    private lateinit var recylcerView: RecyclerView
    private lateinit var selectedAccount: ValorantAccount
    private lateinit var adapter: ValorantAccountAdapter
    private lateinit var accountsDB: ValorantAccountDatabase

    val PERMISSION_REQUEST_CODE = 112

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_selection)

        accountsDB = ValorantAccountDatabase(this)
        val listAccounts = accountsDB.getAllValorantAccounts()
        val rootView = findViewById<View>(android.R.id.content)

        recylcerView = findViewById(R.id.valorant_accounts_recyclerview)
        recylcerView.layoutManager = GridLayoutManager(this, 2)
        adapter = ValorantAccountAdapter(listAccounts, this, this)
        recylcerView.adapter = adapter

        if (listAccounts.isEmpty()) {
            Snackbar.make(
                rootView,
                getString(R.string.add_an_account_to_get_started), Snackbar.LENGTH_SHORT
            ).show()
        }

        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            val intent = Intent(this, NewLogInUI::class.java)
            intent.putExtra("login", "true")
            startActivity(intent)
        }

        if (Build.VERSION.SDK_INT > 32) {
            if (!shouldShowRequestPermissionRationale("112")) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onItemClick(account: ValorantAccount) {
        val cookies = account.cookies
        val intent = Intent(this, NewLogInUI::class.java)
        intent.putExtra("cookies", cookies)
        startActivity(intent)
    }

    override fun onItemLongClick(account: ValorantAccount, position: Int) {
        selectedAccount = account
        showMenu(recylcerView[position])
    }

    private fun showMenu(v: View) {
        PopupMenu(this, v, Gravity.CENTER).apply {
            setOnMenuItemClickListener(this@AccountSelectionActivity)
            inflate(R.menu.actions)
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_removeAccount -> {
                deleteAccount(selectedAccount)
                true
            }

            else -> false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteAccount(account: ValorantAccount) {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle("Remove Account")
        dialog.setMessage("Are you sure you want to remove ${account.name}?")
        dialog.setPositiveButton("Yes") { dialog, which ->
            val database = ValorantAccountDatabase(this)
            if (database.deletePlayer(account.puuid)) {
                adapter = ValorantAccountAdapter(accountsDB.getAllValorantAccounts(), this, this)
                adapter.notifyDataSetChanged()
                recylcerView.adapter = adapter
                Toast.makeText(this, "Removed Account", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        dialog.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        dialog.show()
    }
}
