data class PlayerStat(
    val ability: Ability,
    val damage: List<Damage>,
    val economy: Economy,
    val kills: List<Kill>,
    val puuid: String,
    val score: Int
)