
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.net.URL
import kotlin.properties.Delegates

var MAX_MATCHES = 50
var local by Delegates.notNull<Boolean>()
var agentIDs: HashMap<String, String>? = null

val objectMapper = ObjectMapper()

fun main(args: Array<String>) {
    print("Enter the number of matches to process: ")
    MAX_MATCHES = readlnOrNull()?.toInt() ?: 50
    local = false

    runBlocking {
        createMatchesDirectory()
        getAgents()
        val url = "https://eu.api.riotgames.com/val/match/v1/recent-matches/by-queue/competitive?api_key=RGAPI-77322163-520c-492f-aabe-6c29a39f44ff"
        val matches = processMatches(url, MAX_MATCHES, local)
        val agents = getMostPlayedAgent(matches)
        for (agent in agents) {
            println("${agentIDs?.get(agent.key)}: ${agent.value}")
        }
    }
}

fun HashMap<String, String>.toString() {
    for (agent in this) {
        println("${agent.value}: ${agent.key}")
    }
}

fun getAgents() {
    agentIDs = HashMap()
    val url = "https://valorant-api.com/v1/agents?isPlayableCharacter=true"
    val data = JSONObject(URL(url).readText()).getJSONArray("data")
    for (i in 0 until data.length()) {
        val agent = data.getJSONObject(i)
        agentIDs?.put(agent.getString("uuid"), agent.getString("displayName"))
    }
}

fun getMostPlayedAgent(matches: List<ValorantMatch>): MutableMap<String, Int> {
    var agents = mutableMapOf<String, Int>()
    for (match in matches) {
        for (player in match.players) {
            // check if the agent is in the map, if it is then increment the value by 1 otherwise add it to the map
            if (agents.containsKey(player.characterId)) {
                agents[player.characterId] = agents[player.characterId]!! + 1
            } else {
                agents[player.characterId] = 1
            }
        }
    }
    // sort by value from highest to lowest
    agents = agents.toList().sortedByDescending { (_, value) -> value }.toMap().toMutableMap()
    return agents
}

suspend fun processMatches(url: String, maxMatches: Int? = null, local: Boolean = false): List<ValorantMatch> {
    val matches = mutableListOf<ValorantMatch>()
    if (!local) {
        val json = JSONObject(URL(url).readText())
        val matchesArray = json.getJSONArray("matchIds")
        val range = maxMatches?.coerceAtMost(matchesArray.length()) ?: matchesArray.length()

        // Create a list to store the coroutines
        val coroutines = mutableListOf<Deferred<ValorantMatch>>()

        // Create a coroutine for each match
        for (i in 0 until range) {
            val matchId = matchesArray.getString(i)
            val coroutine = CoroutineScope(Dispatchers.IO).async {
                getMatch(matchId, i)
            }
            println("Getting match $i")
            coroutines.add(coroutine)
        }

        // Await the completion of all coroutines
        matches.addAll(coroutines.awaitAll())
        return matches
    } else {
        println("Processing matches from local files")
        val folder = File("matches")
        for (file in folder.listFiles()!!) {
            val json = JSONObject(file.readText())
            matches.add(Gson().fromJson(json.toString(), ValorantMatch::class.java))
            //println("Match ${file.name} processed")
        }
        return matches
    }
}

fun getMatch(matchID: String, number: Int): ValorantMatch {
    val folder = File("matches")
    // check if the matchID.txt file exists, if it does then read the file and return the match
    if (folder.listFiles()?.any { it.name == "$matchID.txt" } == true) {
        val file = File("matches/$matchID.txt")
        val json = JSONObject(file.readText())
        return Gson().fromJson(json.toString(), ValorantMatch::class.java)
    }
    val url = "https://eu.api.riotgames.com/val/match/v1/matches/$matchID?api_key=RGAPI-77322163-520c-492f-aabe-6c29a39f44ff"
    val json = JSONObject(URL(url).readText())
//    val file = File("matches/$matchID.txt")
//    if (!file.exists()) {
//        file.createNewFile()
//        file.writeText(json.toString())
//    }
    println("Got Match $number")
    //return objectMapper.readValue(json.toString(), ValorantMatch::class.java)
    return Gson().fromJson(json.toString(), ValorantMatch::class.java)
}

fun createMatchesDirectory() {
    val folder = File("matches")
    if (!folder.exists()) {
        folder.mkdirs()
    }
}
