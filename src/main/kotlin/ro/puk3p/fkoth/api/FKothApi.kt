package ro.puk3p.fkoth.api

interface FKothApi {
    fun getWinsForFaction(faction: String): Int

    fun getWinsForPlayerFaction(playerName: String): Int

    fun getFactionForPlayer(playerName: String): String?

    fun addWinsForFaction(
        faction: String,
        amount: Int,
    ): Int

    fun setWinsForFaction(
        faction: String,
        amount: Int,
    ): Int

    fun removeWinsForFaction(
        faction: String,
        amount: Int,
    ): Int

    fun top(limit: Int): List<FactionWinsSnapshot>
}

data class FactionWinsSnapshot(
    val faction: String,
    val wins: Int,
)
