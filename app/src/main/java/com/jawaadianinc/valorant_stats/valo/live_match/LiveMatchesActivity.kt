package com.jawaadianinc.valorant_stats.valo.live_match

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityLiveMatchesBinding
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.*

class LiveMatchesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveMatchesBinding
    private lateinit var nameSplit: List<String>
    private lateinit var codeLayout: ConstraintLayout
    private lateinit var code: String
    private lateinit var gameMode: String

    // a hash map to store the mapID and the map image
    private lateinit var mapHash: HashMap<String, String>
    private var listOfEnemies = ArrayList<ImageView>()
    private var listofAllyTeam = ArrayList<ImageView>()
    private lateinit var unratedBT: Button
    private lateinit var competitiveBT: Button
    private lateinit var spikeRushBT: Button
    private lateinit var deathMatchBT: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveMatchesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  initialize the map hash map
        mapHash = HashMap()
        mapHash["Faheem"] = "Saleem"

        listOfEnemies.add(binding.enemy1)
        listOfEnemies.add(binding.enemy5)
        listOfEnemies.add(binding.enemy2)
        listOfEnemies.add(binding.enemy3)
        listOfEnemies.add(binding.enemy4)

        listofAllyTeam.add(binding.team1)
        listofAllyTeam.add(binding.team5)
        listofAllyTeam.add(binding.team2)
        listofAllyTeam.add(binding.team3)
        listofAllyTeam.add(binding.team4)


        unratedBT = binding.unratedBT
        competitiveBT = binding.compBT
        spikeRushBT = binding.SpikeRushBT
        deathMatchBT = binding.DMBT

        val playerName: String = PlayerDatabase(this).getPlayerName()!!
        nameSplit = playerName.split("#")

        val codeEnter = binding.valorantCodeEnter
        val toolbar = binding.toolbar5
        val submit = binding.submitCodeBT
        codeLayout = binding.CodeSubmissionView


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        supportActionBar?.title = "Enter Code"

        val database = Firebase.database
        val codesRef = database.getReference("VALORANT/LiveMatch/")

        // listen for when the code is entered
        submit.setOnClickListener {
            code = codeEnter.text.toString()
            // check if the code is valid by checking if it is 6 characters long and if it is all numbers
            if (code.length == 6 && code.all { it.isDigit() }) {
                // check if the code exists in the database
                codesRef.child(code).get().addOnSuccessListener {
                    if (it.exists()) {
                        // set the value of the Connected one value higher than the current value
                        codesRef.child(code).child("Connected")
                            .setValue(it.child("Connected").value.toString().toInt() + 1)
                        toolbar.title = "Connecting to $code"
                        doAsync {
                            try {
                                val maps =
                                    JSONObject(URL("https://valorant-api.com/v1/maps").readText()).getJSONArray(
                                        "data"
                                    )
                                for (i in 0 until maps.length()) {
                                    val map = maps.getJSONObject(i)
                                    mapHash[map.getString("mapUrl")] = map.getString("splash")
                                }

                                uiThread {
                                    startListening(code)
                                }
                            } catch (e: Exception) {
                                Log.e("FirebaseLiveMatches", e.toString())
                            }
                        }

                    } else {
                        // if the code does not exist, display an error message
                        codeEnter.error = "Invalid Code entered"
                        toolbar.title = "Enter Code"
                    }
                }
            } else {
                // if the code is not valid, then show an error message
                codeEnter.error = "Invalid Code, must be 6 numbers"
                toolbar.title = "Enter Code"
            }
        }
    }

    private fun startListening(code: String) {
        val mediaPlayer = MediaPlayer()
        val moosicStorage = FirebaseStorage.getInstance().reference
        val moosic = moosicStorage.child("Valorant Music/")

        val database = Firebase.database
        val toolbar = binding.toolbar5

        val mapBackground = binding.mapImageLiveMatches
        val inGameMapBackground = binding.inGameMapImage

        val LiveMatchPlayerList = binding.LiveMatchPlayerList
        val Liveplayers = ArrayList<LiveMatchPlayer>()
        val LiveplayerAdapter = LiveMatchPlayerAdapter(this, Liveplayers)
        LiveMatchPlayerList.adapter = LiveplayerAdapter

        val PartyMembersList = binding.PartyMembersList
        val PartyMembers = ArrayList<PartyMember>()
        val PartyMemberAdapter = PartyMembersAdapter(this, PartyMembers)
        PartyMembersList.adapter = PartyMemberAdapter

        val codeSubmissionView = binding.CodeSubmissionView
        val LobbyMode = binding.LobbyMode
        val PreGameMode = binding.PreGameMode
        val LaunchValorantMode = binding.LoadValorantMode
        val InGameMode = binding.InGameMode

        val InGameAllyList = binding.inGameAllyList
        val InGameAllyPlayers = ArrayList<LiveMatchPlayer>()
        val InGameAllyAdapter = LiveMatchPlayerAdapter(this, InGameAllyPlayers)
        InGameAllyList.adapter = InGameAllyAdapter

        val InGameEnemyList = binding.inGameEnemyList
        val InGameEnemyPlayers = ArrayList<LiveMatchPlayer>()
        val InGameEnemyAdapter = LiveMatchPlayerAdapter(this, InGameEnemyPlayers)
        InGameEnemyList.adapter = InGameEnemyAdapter

        var runPlayerCardsOnce = true

        val AssetDB = AssetsDatabase(this)

        var timerRunning = false
        val findMatchDialog =
            ProgressDialogStatics().setProgressDialog(this, "Waiting for match to start...")
        val codesRef = database.getReference("VALORANT/LiveMatch/")
        val stateOfGame = database.getReference("VALORANT/LiveMatch/$code")

        stateOfGame.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@LiveMatchesActivity, "Live match ended", Toast.LENGTH_SHORT)
                        .show()
                    // check if activity is still existing
                    if (!isFinishing) {
                        finish()
                    }
                } else {
                    binding.valorantCodeEnter.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.valorantCodeEnter.windowToken, 0)
                    codeSubmissionView.visibility = ConstraintLayout.GONE
                }

                // if the game is in the lobby, then show the lobby screen
                when (snapshot.child("State").value) {
                    "LAUNCH_VALORANT" -> {
                        toolbar.title = "Open Valorant"
                        LaunchValorantMode.visibility = ConstraintLayout.VISIBLE
                        LobbyMode.visibility = ConstraintLayout.GONE
                        PreGameMode.visibility = ConstraintLayout.GONE
                        InGameMode.visibility = ConstraintLayout.GONE
                        timerRunning = false
                    }
                    "LOBBY" -> {
                        dimPlayers()
                        InGameAllyPlayers.clear()
                        InGameEnemyPlayers.clear()

                        timerRunning = false
                        findMatchDialog.dismiss()
                        toolbar.title = "In Lobby"

                        LobbyMode.visibility = ConstraintLayout.VISIBLE
                        PreGameMode.visibility = ConstraintLayout.GONE
                        LaunchValorantMode.visibility = ConstraintLayout.GONE
                        InGameMode.visibility = ConstraintLayout.GONE


                        if (snapshot.child("Mode").value == "None") {
                            lobbyEnableButtons()
                        } else {
                            lobbyDisableButtons()
                        }

                        unratedBT.setOnClickListener {
                            codesRef.child(code).child("Mode").setValue("Unrated")
                            lobbyDisableButtons()

                        }
                        competitiveBT.setOnClickListener {
                            codesRef.child(code).child("Mode").setValue("Competitive")
                            lobbyDisableButtons()
                        }
                        spikeRushBT.setOnClickListener {
                            codesRef.child(code).child("Mode").setValue("Spike Rush")
                            lobbyDisableButtons()
                        }
                        deathMatchBT.setOnClickListener {
                            codesRef.child(code).child("Mode").setValue("DeathMatch")
                            lobbyDisableButtons()
                        }

                        val playerName = snapshot.child("Player").value.toString()
                        if (runPlayerCardsOnce) {
                            try {
                                getPlayerBackground(playerName)
                                runPlayerCardsOnce = false
                            } catch (e: Exception) {
                                val errorRef =
                                    database.getReference("VALORANT/LiveMatch/$code/Errors")
                                errorRef.push().setValue(e.toString())
                            }
                        }

                        val partyMembers = snapshot.child("DEFAULT").child("Members").children
                        PartyMembers.clear()
                        for (member in partyMembers) {
                            val playerIdentity = member.child("PlayerIdentity")
                            val level = playerIdentity.child("AccountLevel").value.toString()
                            val playerCard = playerIdentity.child("PlayerCardID").value.toString()
                            val playerTitle = playerIdentity.child("PlayerTitleID").value.toString()

                            doAsync {
                                val cardURL =
                                    "https://valorant-api.com/v1/playercards/${playerCard}"
                                val titleURL =
                                    "https://valorant-api.com/v1/playertitles/${playerTitle}"

                                val cardJSON = URL(cardURL).readText()
                                val titleJSON = URL(titleURL).readText()

                                val cardName =
                                    JSONObject(cardJSON).getJSONObject("data").getString("largeArt")
                                val titleName = JSONObject(titleJSON).getJSONObject("data")
                                    .getString("titleText")

                                uiThread {
                                    val PartyPlayer = PartyMember(
                                        cardName, titleName, level.toInt()
                                    )
                                    // check if the player is already in the list
                                    if (!PartyMembers.contains(PartyPlayer)) {
                                        PartyMembers.add(PartyPlayer)
                                        PartyMemberAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                        }

                        // clear the LivePlayers list
                        Liveplayers.clear()

                    }
                    "LOOKING_FOR_MATCH" -> {
                        //LobbyMode.visibility = ConstraintLayout.GONE
                        PreGameMode.visibility = ConstraintLayout.GONE
                        LaunchValorantMode.visibility = ConstraintLayout.GONE
                        InGameMode.visibility = ConstraintLayout.GONE

                        // get value of Mode
                        val mode = database.getReference("VALORANT/LiveMatch/$code/Mode")
                        mode.get().addOnSuccessListener {
                            toolbar.title = "Finding ${it.value} match"
                        }
                        findMatchDialog.show()
                    }
                    "PREGAME_CHARACTER_SELECT" -> {
                        LobbyMode.visibility = ConstraintLayout.GONE
                        PreGameMode.visibility = ConstraintLayout.VISIBLE
                        LaunchValorantMode.visibility = ConstraintLayout.GONE
                        InGameMode.visibility = ConstraintLayout.GONE

                        if (!mediaPlayer.isPlaying) {
                            moosic.listAll().addOnSuccessListener { it ->
                                val random = (0 until it.items.size).random()
                                moosic.child(it.items[random].name).getBytes(10000 * 10000)
                                    .addOnSuccessListener {
                                        mediaPlayer.reset()
                                        val file = File.createTempFile("temp", "mp3")
                                        file.writeBytes(it)
                                        mediaPlayer.setDataSource(file.absolutePath)
                                        mediaPlayer.prepare()
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener {
                                            mediaPlayer.reset()
                                        }
                                    }
                            }
                        }

                        // if the game is in the match, then show the match screen
                        findMatchDialog.dismiss()
                        // get the child of PreGame
                        try {
                            val mapID = snapshot.child("Pregame").child("MapID").value.toString()
                            // get the map image from the map id with the hash
                            val mapImage = mapHash[mapID]
                            // set the map image
                            Picasso.get().load(mapImage).into(mapBackground)

                            val secondsLeft =
                                snapshot.child("Pregame")
                                    .child("PhaseTimeRemainingNS").value.toString()
                                    .toLong()
                            val seconds = secondsLeft / 1000000000

                            // start a timer with the seconds left
                            val secondsleftText = binding.secondsLeft
                            val timer = object : CountDownTimer(seconds * 1000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    if (!timerRunning) {
                                        cancel()
                                    } else {
                                        val secondsLeft = millisUntilFinished / 1000
                                        secondsleftText.text = secondsLeft.toString()
                                    }
                                }

                                override fun onFinish() {
                                    secondsleftText.text = "0"
                                }
                            }

                            if (!timerRunning) {
                                timer.start()
                                timerRunning = true
                            }

                            // if the value is less than 0, then the game is in the match
                            if (secondsLeft == 0L) {
                                timer.cancel()
                                timerRunning = false
                                secondsleftText.text = "0"
                            }

                            val enemiesLockCount = snapshot.child("Pregame")
                                .child("EnemyTeamLockCount").value.toString().toInt()
                            for (i in 0 until enemiesLockCount) {
                                listOfEnemies[i].alpha = 1f
                            }

                            val allyTeam =
                                snapshot.child("Pregame").child("AllyTeam").child("Players")
                            val team = snapshot.child("Pregame").child("AllyTeam")
                                .child("TeamID").value.toString()

                            if (team == "Red") {
                                // make list of allies red and enemies blue
                                for (i in 0 until 5) {
                                    // get the valorant_red colour
                                    val allyColour = ContextCompat.getColor(
                                        this@LiveMatchesActivity,
                                        R.color.Valorant_Red
                                    )
                                    listofAllyTeam[i].setBackgroundColor(allyColour)
                                    val enemycolour = ContextCompat.getColor(
                                        this@LiveMatchesActivity,
                                        R.color.Valorant_Blue
                                    )
                                    listOfEnemies[i].setBackgroundColor(enemycolour)
                                }
                            } else {
                                // make list of allies blue and enemies red
                                for (i in 0 until 5) {
                                    val allyColour = ContextCompat.getColor(
                                        this@LiveMatchesActivity,
                                        R.color.Valorant_Blue
                                    )
                                    listofAllyTeam[i].setBackgroundColor(allyColour)
                                    val enemycolour = ContextCompat.getColor(
                                        this@LiveMatchesActivity,
                                        R.color.Valorant_Red
                                    )
                                    listOfEnemies[i].setBackgroundColor(enemycolour)
                                }
                            }

                            Liveplayers.clear()
                            // loop through the players
                            for (i in 0 until allyTeam.childrenCount.toInt()) {
                                val player = allyTeam.child(i.toString())
                                val characterID = player.child("CharacterID").value.toString()
                                if (characterID != "") {
                                    val agentImage = AssetDB.retrieveImageUUID(characterID)
                                    listofAllyTeam[i].setImageBitmap(agentImage)
                                    if (player.child("CharacterSelectionState").value.toString() == "locked") {
                                        listofAllyTeam[i].alpha = 1f
                                        val agentName = AssetDB.retrieveName(characterID)
                                        val livePlayer =
                                            LiveMatchPlayer(agentName, agentImage, team)
                                        Liveplayers.add(livePlayer)
                                        LiveplayerAdapter.notifyDataSetChanged()
                                    }
                                }
                            }

                            val toolbar = binding.toolbar5
                            val gameMode = binding.gameModeLiveMatches

                            val mode = database.getReference("VALORANT/LiveMatch/$code/Mode")
                            mode.get().addOnSuccessListener {
                                val modeText = it.value.toString()
                                // capitalize the first letter of the mode
                                val modeTextCapitalized = modeText.substring(0, 1).uppercase(
                                    Locale.ROOT
                                ) + modeText.substring(1)
                                gameMode.text = modeTextCapitalized
                            }

                            toolbar.title = "Choosing agents"

                        } catch (E: Exception) {
                            Log.d("FirebaseLiveMatches", "Error: $E")
                        }
                    }

                    "PREGAME_STARTING" -> {
                        LobbyMode.visibility = ConstraintLayout.GONE
                        //PreGameMode.visibility = ConstraintLayout.GONE
                        LaunchValorantMode.visibility = ConstraintLayout.GONE
                        InGameMode.visibility = ConstraintLayout.GONE

                        val secondsleftText = binding.secondsLeft
                        secondsleftText.text = "0"

                        timerRunning = false

                        val EnemyTeam =
                            snapshot.child("Pregame").child("EnemyTeam").child("Players")
                        for (i in 0 until EnemyTeam.childrenCount.toInt()) {
                            val player = EnemyTeam.child(i.toString())
                            val characterID = player.child("CharacterID").value.toString()
                            if (characterID != "") {
                                val agentImage = AssetDB.retrieveImageUUID(characterID)
                                listOfEnemies[i].setImageBitmap(agentImage)
                                listOfEnemies[i].alpha = 1f
                            }
                        }

                        findMatchDialog.dismiss()
                        toolbar.title = "Starting match"
                    }
                    "INGAME" -> {
                        LobbyMode.visibility = ConstraintLayout.GONE
                        PreGameMode.visibility = ConstraintLayout.GONE
                        LaunchValorantMode.visibility = ConstraintLayout.GONE
                        InGameMode.visibility = ConstraintLayout.VISIBLE

                        val mapID = snapshot.child("Ingame").child("MapID").value.toString()
                        val mapImage = mapHash[mapID]
                        Picasso.get().load(mapImage).into(inGameMapBackground)
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.stop()
                        }

                        val allyTeam = snapshot.child("Pregame").child("AllyTeam")
                            .child("TeamID").value.toString()

                        val playersAlly =
                            snapshot.child("Pregame").child("AllyTeam").child("Players")
                        // loop through the players
                        InGameAllyPlayers.clear()
                        for (i in 0 until playersAlly.childrenCount.toInt()) {
                            val player = playersAlly.child(i.toString())
                            val characterID = player.child("CharacterID").value.toString()
                            val characterName = AssetDB.retrieveName(characterID)
                            val agentImage = AssetDB.retrieveImageUUID(characterID)
                            val livePlayer = LiveMatchPlayer(characterName, agentImage, allyTeam)
                            InGameAllyPlayers.add(livePlayer)
                            InGameAllyAdapter.notifyDataSetChanged()
                        }

                        val playersEnemy =
                            snapshot.child("Pregame").child("EnemyTeam").child("Players")
                        // loop through the players
                        InGameEnemyPlayers.clear()
                        for (i in 0 until playersEnemy.childrenCount.toInt()) {
                            val player = playersEnemy.child(i.toString())
                            val characterID = player.child("CharacterID").value.toString()
                            val characterName = AssetDB.retrieveName(characterID)
                            // opposite of ally team
                            val enemyTeam = if (allyTeam == "Red") "Blue" else "Red"
                            val agentImage = AssetDB.retrieveImageUUID(characterID)
                            val livePlayer = LiveMatchPlayer(characterName, agentImage, enemyTeam)
                            InGameEnemyPlayers.add(livePlayer)
                            InGameEnemyAdapter.notifyDataSetChanged()
                        }

                        val allyScore = snapshot.child("MatchActivity").child("RoundData")
                            .child("partyOwnerMatchScoreAllyTeam").value.toString()
                        val enemyScore = snapshot.child("MatchActivity").child("RoundData")
                            .child("partyOwnerMatchScoreEnemyTeam").value.toString()

                        val scoreText = "$allyScore - $enemyScore"
                        val detailsText = binding.inGameRoundDetails
                        detailsText.text = scoreText
                        findMatchDialog.dismiss()
                        val mode = snapshot.child("Ingame").child("MatchmakingData")
                            .child("QueueID").value.toString()
                        // capitalize the first letter of the mode
                        val modeTextCapitalized = mode.substring(0, 1).uppercase(
                            Locale.ROOT
                        ) + mode.substring(1)
                        toolbar.title = "Playing $modeTextCapitalized"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LiveMatchesActivity, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        disconnectCode()
    }


    private fun lobbyEnableButtons() {
        val unratedBT = binding.unratedBT
        val competitiveBT = binding.compBT
        val spikeRushBT = binding.SpikeRushBT
        val deathMatchBT = binding.DMBT

        unratedBT.alpha = 1F
        competitiveBT.alpha = 1F
        spikeRushBT.alpha = 1F
        deathMatchBT.alpha = 1F
        unratedBT.isClickable = true
        competitiveBT.isClickable = true
        spikeRushBT.isClickable = true
        deathMatchBT.isClickable = true
    }

    private fun lobbyDisableButtons() {
        val unratedBT = binding.unratedBT
        val competitiveBT = binding.compBT
        val spikeRushBT = binding.SpikeRushBT
        val deathMatchBT = binding.DMBT

        unratedBT.alpha = 0.5F
        competitiveBT.alpha = 0.5F
        spikeRushBT.alpha = 0.5F
        deathMatchBT.alpha = 0.5F
        unratedBT.isClickable = false
        competitiveBT.isClickable = false
        spikeRushBT.isClickable = false
        deathMatchBT.isClickable = false
    }

    private fun dimPlayers() {
        for (i in 0 until listOfEnemies.size) {
            listOfEnemies[i].alpha = 0.5f
        }
        for (i in 0 until listofAllyTeam.size) {
            listofAllyTeam[i].alpha = 0.5f
        }
    }

    private fun disconnectCode() {
        // check if code has been initialized
        try {
            val database = Firebase.database
            val codesRef = database.getReference("VALORANT/LiveMatch/$code/Connected")
            // set the connected value one less than it was
            codesRef.get().addOnSuccessListener {
                val connected = it.value.toString().toInt()
                codesRef.setValue(connected - 1)
            }
        } catch (e: UninitializedPropertyAccessException) {
            // do nothing
        } catch (e: Exception) {
            // to firebase
            FirebaseDatabase.getInstance().reference.child("LiveMatchErrors").push()
                .setValue(e.toString())
        }
    }

    private fun getPlayerBackground(PlayerName: String) {
        val name = PlayerName.split("#")[0]
        val tag = PlayerName.split("#")[1]
        val image = binding.playerImageBackround

        doAsync {
            val data =
                Henrik(this@LiveMatchesActivity).henrikAPI("https://api.henrikdev.xyz/valorant/v1/account/${name}/${tag}?force=true")["data"] as JSONObject
            val largePic = data.getJSONObject("card").getString("large") as String
            uiThread {
                Picasso.get().load(largePic)
                    .transform(BlurTransformation(this@LiveMatchesActivity)).fit().centerInside()
                    .into(image)
            }
        }
    }

}
