data class MatchInfo(
    val customGameName: String,
    val gameLengthMillis: Int,
    val gameMode: String,
    val gameStartMillis: Long,
    val gameVersion: String,
    val isCompleted: Boolean,
    val isRanked: Boolean,
    val mapId: String,
    val matchId: String,
    val provisioningFlowId: String,
    val queueId: String,
    val region: String,
    val seasonId: String
)