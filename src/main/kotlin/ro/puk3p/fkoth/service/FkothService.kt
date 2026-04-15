package ro.puk3p.fkoth.service

import org.bukkit.entity.Player
import ro.puk3p.fkoth.integration.factions.FactionsAdapter
import ro.puk3p.fkoth.model.FactionWins
import ro.puk3p.fkoth.storage.FactionWinsRepository

enum class AddByPlayerResult {
    SUCCESS,
    NO_FACTION
}

class FkothService(
    private val repository: FactionWinsRepository,
    private val factionsAdapter: FactionsAdapter
) {

    fun addWinsForPlayer(playerName: String, amount: Int): Pair<AddByPlayerResult, String?> {
        val faction = factionsAdapter.getFactionNameByPlayerName(playerName)
            ?: return AddByPlayerResult.NO_FACTION to null

        repository.addWins(faction, amount)
        return AddByPlayerResult.SUCCESS to faction
    }

    fun getFactionForPlayer(player: Player): String? {
        return factionsAdapter.getFactionNameByPlayerUuid(player.uniqueId)
    }

    fun getWinsForFaction(faction: String): Int = repository.getWins(faction)

    fun addWinsForFaction(faction: String, amount: Int): Int = repository.addWins(faction, amount)

    fun removeWinsForFaction(faction: String, amount: Int): Int = repository.removeWins(faction, amount)

    fun setWinsForFaction(faction: String, amount: Int): Int = repository.setWins(faction, amount)

    fun clearFaction(faction: String) = repository.removeFaction(faction)

    fun top(limit: Int): List<FactionWins> = repository.top(limit)

    fun save() = repository.save()
}
