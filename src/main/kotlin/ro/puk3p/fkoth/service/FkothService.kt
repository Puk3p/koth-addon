package ro.puk3p.fkoth.service

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ro.puk3p.fkoth.integration.factions.FactionsAdapter
import ro.puk3p.fkoth.model.FactionWins
import ro.puk3p.fkoth.storage.FactionWinsRepository

enum class AddByPlayerResult {
    SUCCESS,
    PLAYER_OFFLINE,
    NO_FACTION
}

data class FkothRules(
    val ignoreNoFactionWinner: Boolean,
    val allowOfflinePlayerLookup: Boolean
)

class FkothService(
    private val repository: FactionWinsRepository,
    private val factionsAdapter: FactionsAdapter,
    private val rules: FkothRules
) {

    fun addWinsForPlayer(playerName: String, amount: Int): Pair<AddByPlayerResult, String?> {
        if (!rules.allowOfflinePlayerLookup && Bukkit.getPlayerExact(playerName) == null) {
            return AddByPlayerResult.PLAYER_OFFLINE to null
        }

        val faction = factionsAdapter.getFactionNameByPlayerName(playerName)
            ?: run {
                if (!rules.ignoreNoFactionWinner) {
                    val wildernessName = "Wilderness"
                    repository.addWins(wildernessName, amount)
                    return AddByPlayerResult.SUCCESS to wildernessName
                }
                return AddByPlayerResult.NO_FACTION to null
            }

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

    fun getRankForFaction(faction: String): Int? {
        val ranked = repository.top(Int.MAX_VALUE)
        val idx = ranked.indexOfFirst { it.faction.equals(faction, ignoreCase = true) }
        return if (idx == -1) null else idx + 1
    }

    fun getTrackedFactionCount(): Int = repository.top(Int.MAX_VALUE).size

    fun save() = repository.save()
}
