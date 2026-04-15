package ro.puk3p.fkoth.integration.koth

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import ro.puk3p.fkoth.service.AddByPlayerResult
import ro.puk3p.fkoth.service.FkothService

class KothHookListener(
    private val plugin: JavaPlugin,
    private val service: FkothService,
    private val kothPluginName: String,
    private val endEventClassName: String,
    private val winnerPaths: List<String>
) : Listener {

    fun register(): Boolean {
        if (plugin.server.pluginManager.getPlugin(kothPluginName) == null) {
            return false
        }

        val clazz = runCatching { Class.forName(endEventClassName) }.getOrNull() ?: return false
        if (!Event::class.java.isAssignableFrom(clazz)) {
            return false
        }

        @Suppress("UNCHECKED_CAST")
        val eventClass = clazz as Class<out Event>

        plugin.server.pluginManager.registerEvent(eventClass, this, EventPriority.MONITOR, { _, event ->
            handleKothEnd(event)
        }, plugin, true)

        return true
    }

    private fun handleKothEnd(event: Event) {
        val winnerName = resolveWinnerName(event)
        if (winnerName.isNullOrBlank()) {
            plugin.logger.warning("[FKoth] KoTH ended but winner could not be resolved from event: ${event.javaClass.name}")
            return
        }

        val (status, faction) = service.addWinsForPlayer(winnerName, 1)
        when (status) {
            AddByPlayerResult.SUCCESS -> {
                plugin.logger.info("[FKoth] Added 1 win to faction '$faction' (winner: $winnerName)")
            }

            AddByPlayerResult.NO_FACTION -> {
                plugin.logger.info("[FKoth] Winner '$winnerName' has no faction, win ignored.")
            }
        }
    }

    private fun resolveWinnerName(event: Event): String? {
        for (path in winnerPaths) {
            val raw = resolvePath(event, path)
            val name = toPlayerName(raw)
            if (!name.isNullOrBlank()) {
                return name
            }
        }
        return null
    }

    private fun resolvePath(root: Any, path: String): Any? {
        var current: Any? = root
        for (methodName in path.split('.')) {
            if (current == null) {
                return null
            }
            current = callNoArg(current, methodName)
        }
        return current
    }

    private fun toPlayerName(value: Any?): String? {
        return when (value) {
            null -> null
            is String -> value
            is Player -> value.name
            is OfflinePlayer -> value.name
            else -> callNoArg(value, "getName")?.toString()
        }
    }

    private fun callNoArg(target: Any, methodName: String): Any? {
        return runCatching {
            val method = target.javaClass.methods.firstOrNull { it.name == methodName && it.parameterCount == 0 }
                ?: target.javaClass.declaredMethods.firstOrNull { it.name == methodName && it.parameterCount == 0 }
                ?: return null
            method.isAccessible = true
            method.invoke(target)
        }.getOrNull()
    }
}
