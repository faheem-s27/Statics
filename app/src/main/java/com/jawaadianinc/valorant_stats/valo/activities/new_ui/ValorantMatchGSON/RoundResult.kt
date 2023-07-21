data class RoundResult(
    val bombDefuser: String?,
    val bombPlanter: String?,
    val defuseLocation: DefuseLocation,
    val defusePlayerLocations: List<DefusePlayerLocation>?,
    val defuseRoundTime: Int,
    val plantLocation: PlantLocation,
    val plantPlayerLocations: List<PlantPlayerLocation>?,
    val plantRoundTime: Int,
    val plantSite: String,
    val playerStats: List<PlayerStat>,
    val roundCeremony: String,
    val roundNum: Int,
    val roundResult: String,
    val roundResultCode: String,
    val winningTeam: String
)