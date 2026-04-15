package ro.puk3p.fkoth.integration.placeholder

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import ro.puk3p.fkoth.service.FkothService

class FKothPlaceholderExpansion(
    private val service: FkothService
) : PlaceholderExpansion() {

    private val topRegex = Regex("top_(\\d+)_(name|wins)")

    override fun getIdentifier(): String = "fkoth"

    override fun getAuthor(): String = "puk3p"

    override fun getVersion(): String = "1.0.0"

    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        if (params.equals("top_size", ignoreCase = true)) {
            return service.getTrackedFactionCount().toString()
        }

        if (params.equals("faction_name", ignoreCase = true)) {
            if (player == null) {
                return "-"
            }
            return service.getFactionForPlayer(player) ?: "-"
        }

        if (params.equals("faction_wins", ignoreCase = true)) {
            if (player == null) {
                return "0"
            }
            val faction = service.getFactionForPlayer(player) ?: return "0"
            return service.getWinsForFaction(faction).toString()
        }

        if (params.equals("faction_rank", ignoreCase = true)) {
            if (player == null) {
                return "-"
            }
            val faction = service.getFactionForPlayer(player) ?: return "-"
            return service.getRankForFaction(faction)?.toString() ?: "-"
        }

        val match = topRegex.matchEntire(params.lowercase()) ?: return ""
        val position = match.groupValues[1].toIntOrNull() ?: return ""
        val valueType = match.groupValues[2]

        val top = service.top(position)
        val entry = top.getOrNull(position - 1) ?: return if (valueType == "wins") "0" else "-"

        return if (valueType == "name") entry.faction else entry.wins.toString()
    }
}
