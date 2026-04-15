package ro.puk3p.fkoth.integration.factions

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class FactionsUUIDAdapter(
    private val plugin: JavaPlugin
) : FactionsAdapter {

    private val fPlayersClass: Class<*>? = runCatching {
        Class.forName("com.massivecraft.factions.FPlayers")
    }.getOrNull()

    override fun isAvailable(): Boolean {
        return plugin.server.pluginManager.getPlugin("Factions") != null && fPlayersClass != null
    }

    override fun getFactionNameByPlayerName(playerName: String): String? {
        val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
        val byUuid = offlinePlayer?.uniqueId?.let { getFactionNameByPlayerUuid(it) }
        if (byUuid != null) {
            return byUuid
        }

        val fPlayer = getFPlayerByOfflinePlayer(offlinePlayer)
        return extractFactionName(fPlayer)
    }

    override fun getFactionNameByPlayerUuid(uuid: UUID): String? {
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        val fPlayer = getFPlayerByOfflinePlayer(offlinePlayer)
            ?: getFPlayerById(uuid.toString())
        return extractFactionName(fPlayer)
    }

    private fun getFPlayersSingleton(): Any? {
        val clazz = fPlayersClass ?: return null
        return callFirst(clazz, listOf("getInstance", "i"))
    }

    private fun getFPlayerByOfflinePlayer(offlinePlayer: OfflinePlayer): Any? {
        val singleton = getFPlayersSingleton() ?: return null
        return callFirst(singleton, listOf("getByOfflinePlayer", "getByPlayer"), offlinePlayer)
    }

    private fun getFPlayerById(id: String): Any? {
        val singleton = getFPlayersSingleton() ?: return null
        return callFirst(singleton, listOf("getById", "getByPlayerId"), id)
    }

    private fun extractFactionName(fPlayer: Any?): String? {
        if (fPlayer == null) {
            return null
        }

        val faction = callFirst(fPlayer, listOf("getFaction")) ?: return null
        val isWilderness = (callFirst(faction, listOf("isWilderness")) as? Boolean) ?: false
        val tag = callFirst(faction, listOf("getTag"))?.toString()?.trim().orEmpty()
        if (isWilderness || tag.isEmpty() || tag.equals("wilderness", ignoreCase = true)) {
            return null
        }
        return tag
    }

    private fun callFirst(target: Any, methodNames: List<String>, vararg args: Any?): Any? {
        for (name in methodNames) {
            val result = callMethod(target, name, *args)
            if (result.isSuccess) {
                return result.getOrNull()
            }
        }
        return null
    }

    private fun callMethod(target: Any, methodName: String, vararg args: Any?): Result<Any?> {
        return runCatching {
            val clazz = if (target is Class<*>) target else target.javaClass
            val method = clazz.methods.firstOrNull { it.name == methodName && it.parameterTypes.size == args.size }
                ?: clazz.declaredMethods.firstOrNull { it.name == methodName && it.parameterTypes.size == args.size }
                ?: throw NoSuchMethodException("$methodName/${args.size} not found on ${clazz.name}")

            method.isAccessible = true
            if (target is Class<*>) method.invoke(null, *args) else method.invoke(target, *args)
        }
    }
}
