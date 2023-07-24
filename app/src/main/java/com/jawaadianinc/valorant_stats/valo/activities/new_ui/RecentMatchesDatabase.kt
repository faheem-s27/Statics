import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RecentMatchesDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "valorant_match.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "matches"
        private const val COLUMN_MATCH_ID = "match_id"
        private const val COLUMN_MATCH_DATA = "match_data"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_MATCH_ID TEXT PRIMARY KEY,
                $COLUMN_MATCH_DATA TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert a single match into the database
    suspend fun insertMatch(match: ValorantMatch) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_MATCH_ID, match.matchInfo.matchId)
            put(COLUMN_MATCH_DATA, match.toString())
        }
        db.insert(TABLE_NAME, null, contentValues)
    }

    // Insert a single match into the database
    suspend fun insertMatch2(match: JSONObject) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_MATCH_ID, match.getJSONObject("matchInfo").getString("matchId"))
            put(COLUMN_MATCH_DATA, match.toString())
        }
        db.insert(TABLE_NAME, null, contentValues)
    }

    // Get all matches from the database
    suspend fun getAllMatches(): List<ValorantMatch> = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val matches = mutableListOf<ValorantMatch>()
        val matchDataIndex = cursor.getColumnIndex(COLUMN_MATCH_DATA)

        cursor.use {
            while (it.moveToNext()) {
                val matchData = if (matchDataIndex != -1) it.getString(matchDataIndex) else ""
                try {
                    val valorantMatch = Gson().fromJson(matchData, ValorantMatch::class.java)
                    if (valorantMatch != null) {
                        matches.add(valorantMatch)
                    }
                } catch (e: Exception) {
                    // If Gson deserialization fails, you can log the error or skip adding this match to the list
                    // Log.e("ValorantMatchDatabase", "Failed to deserialize match data", e)
                    // or
                    continue
                }
            }
        }

        matches
    }

    // Get all matches from the database as a coroutine flow
    fun getAllMatchesFlow(): Flow<ValorantMatch> = flow {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        val matchDataIndex = cursor.getColumnIndex(COLUMN_MATCH_DATA)

        cursor.use {
            while (it.moveToNext()) {
                val matchData = if (matchDataIndex != -1) it.getString(matchDataIndex) else ""
                try {
                    val valorantMatch = Gson().fromJson(matchData, ValorantMatch::class.java)
                    if (valorantMatch != null) {
                        emit(valorantMatch)
                    }
                } catch (e: Exception) {
                    // If Gson deserialization fails, you can log the error or skip adding this match to the list
                    // Log.e("ValorantMatchDatabase", "Failed to deserialize match data", e)
                    // or
                    continue
                }
            }
        }
    }

    // Method to check if a matchID exists in the database
    suspend fun matchExists(matchId: String): Boolean = withContext(Dispatchers.IO) {
        val db = readableDatabase

        val selection = "$COLUMN_MATCH_ID = ?"
        val selectionArgs = arrayOf(matchId)

        val query = "SELECT 1 FROM $TABLE_NAME WHERE $selection"

        var matchExists = false
        db.rawQuery(query, selectionArgs)?.use { cursor ->
            matchExists = cursor.moveToFirst()
        }
        matchExists
    }

    // Method to get the total count of matches in the database
    suspend fun getTotalMatchesCount(): Int = withContext(Dispatchers.IO) {
        val db = readableDatabase

        val query = "SELECT COUNT(*) FROM $TABLE_NAME"

        var totalCount = 0
        db.rawQuery(query, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                totalCount = cursor.getInt(0)
            }
        }
        totalCount
    }

    // Delete all matches from the database
    suspend fun deleteAllMatches() = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
    }
}
