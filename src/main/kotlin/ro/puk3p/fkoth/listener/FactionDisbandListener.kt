package ro.puk3p.fkoth.listener

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import ro.puk3p.fkoth.service.FkothService

class FactionDisbandListener(
    private val plugin: JavaPlugin,
    private val service: FkothService,
    private val eventClassName: String
) : Listener {

    fun register(): Boolean {
        val clazz = runCatching { Class.forName(eventClassName) }.getOrNull() ?: return false
        if (!Event::class.java.isAssignableFrom(clazz)) {
            return false
        }

        @Suppress("UNCHECKED_CAST")
        val eventClass = clazz as Class<out Event>

        plugin.server.pluginManager.registerEvent(eventClass, this, EventPriority.MONITOR, { _, event ->
            handleDisband(event)
        }, plugin, true)

        return true
    }

    private fun handleDisband(event: Event) {
        val faction = call(event, "getFaction") ?: return
        val tag = call(faction, "getTag")?.toString()?.trim().orEmpty()
        if (tag.isEmpty()) {
            return
        }

        service.clearFaction(tag)
        plugin.logger.info("[FKoth] Faction disband detected. Removed KOTH wins for faction: $tag")
    }

    private fun call(target: Any, method: String): Any? {
        return runCatching {
            val m = target.javaClass.methods.firstOrNull { it.name == method && it.parameterCount == 0 }
                ?: target.javaClass.declaredMethods.firstOrNull { it.name == method && it.parameterCount == 0 }
                ?: return null
            m.isAccessible = true
            m.invoke(target)
        }.getOrNull()
    }
}
