package ro.puk3p.fkoth.api

import ro.puk3p.fkoth.service.FkothService

class FKothApiImpl(
    private val service: FkothService,
) : FKothApi {
    override fun getWinsForFaction(faction: String): Int = service.getWinsForFaction(faction)

    override fun getWinsForPlayerFaction(playerName: String): Int {
        val faction = service.getFactionForPlayerName(playerName) ?: return 0
        return service.getWinsForFaction(faction)
    }

    override fun getFactionForPlayer(playerName: String): String? = service.getFactionForPlayerName(playerName)

    override fun addWinsForFaction(
        faction: String,
        amount: Int,
    ): Int = service.addWinsForFaction(faction, amount)

    override fun setWinsForFaction(
        faction: String,
        amount: Int,
    ): Int = service.setWinsForFaction(faction, amount)

    override fun removeWinsForFaction(
        faction: String,
        amount: Int,
    ): Int = service.removeWinsForFaction(faction, amount)

    override fun top(limit: Int): List<FactionWinsSnapshot> {
        return service.top(limit).map { FactionWinsSnapshot(it.faction, it.wins) }
    }
}
