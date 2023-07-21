data class Kill(
    val assistants: List<String>,
    val finishingDamage: FinishingDamage,
    val killer: String,
    val playerLocations: List<PlayerLocation>,
    val timeSinceGameStartMillis: Int,
    val timeSinceRoundStartMillis: Int,
    val victim: String,
    val victimLocation: VictimLocation
)