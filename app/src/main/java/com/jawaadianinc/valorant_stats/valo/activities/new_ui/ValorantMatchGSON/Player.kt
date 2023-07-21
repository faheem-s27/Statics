data class Player(
    val characterId: String,
    val competitiveTier: Int,
    val gameName: String,
    val isObserver: Boolean,
    val partyId: String,
    val playerCard: String,
    val playerTitle: String,
    val puuid: String,
    val stats: Stats,
    val tagLine: String,
    val teamId: String
)