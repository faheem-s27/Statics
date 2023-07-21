data class ValorantMatch(
    val coaches: List<Any>,
    val matchInfo: MatchInfo,
    val players: List<Player>,
    val roundResults: List<RoundResult>,
    val teams: List<Team>
)