package ro.puk3p.fkoth.integration.hologram

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import ro.puk3p.fkoth.config.ConfigKeys
import ro.puk3p.fkoth.service.FkothService
import ro.puk3p.fkoth.util.ColorUtil

class TopHologramHook(
    private val plugin: JavaPlugin,
    private val service: FkothService
) {

    private var taskId: Int = -1
    private var warnedMissingApi: Boolean = false

    fun start(): Boolean {
        if (!plugin.config.getBoolean(ConfigKeys.TOP_HOLOGRAM_ENABLED, true)) {
            return false
        }

        if (plugin.server.pluginManager.getPlugin("DecentHolograms") == null) {
            plugin.logger.info("[FKoth] DecentHolograms not found. Top hologram hook disabled.")
            return false
        }

        updateHologram()

        val refreshSeconds = plugin.config.getLong(ConfigKeys.TOP_HOLOGRAM_REFRESH_SECONDS, 15L).coerceAtLeast(5L)
        taskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, Runnable {
            updateHologram()
        }, refreshSeconds * 20L, refreshSeconds * 20L)

        plugin.logger.info("[FKoth] Top hologram hook enabled (refresh: ${refreshSeconds}s).")
        return true
    }

    fun stop() {
        if (taskId != -1) {
            plugin.server.scheduler.cancelTask(taskId)
            taskId = -1
        }
    }

    private fun updateHologram() {
        val location = getLocation() ?: return
        val lines = buildLines()
        upsertDecentHologram(location, lines)
    }

    private fun getLocation(): Location? {
        val worldName = plugin.config.getString(ConfigKeys.TOP_HOLOGRAM_LOCATION_WORLD, "world") ?: return null
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            plugin.logger.warning("[FKoth] Top hologram world not found: $worldName")
            return null
        }

        val x = plugin.config.getDouble(ConfigKeys.TOP_HOLOGRAM_LOCATION_X, 0.5)
        val y = plugin.config.getDouble(ConfigKeys.TOP_HOLOGRAM_LOCATION_Y, 70.0)
        val z = plugin.config.getDouble(ConfigKeys.TOP_HOLOGRAM_LOCATION_Z, 0.5)
        return Location(world, x, y, z)
    }

    private fun buildLines(): List<String> {
        val title = plugin.config.getString(ConfigKeys.TOP_HOLOGRAM_TITLE, "&6&lTop 5 KOTH Factions")
            ?: "&6&lTop 5 KOTH Factions"

        val entries = plugin.config.getInt(ConfigKeys.TOP_HOLOGRAM_ENTRIES, 5).coerceIn(1, 10)
        val top = service.top(entries)

        val lineFormat = plugin.config.getString(
            ConfigKeys.TOP_HOLOGRAM_LINE_FORMAT,
            "&e#{position}. &f{faction} &7- &6{wins}"
        ) ?: "&e#{position}. &f{faction} &7- &6{wins}"

        val emptyLineFormat = plugin.config.getString(
            ConfigKeys.TOP_HOLOGRAM_EMPTY_LINE_FORMAT,
            "&e#{position}. &7-"
        ) ?: "&e#{position}. &7-"

        val lines = mutableListOf(ColorUtil.colorize(title))
        for (index in 1..entries) {
            val item = top.getOrNull(index - 1)
            val line = if (item == null) {
                emptyLineFormat
                    .replace("{position}", index.toString())
            } else {
                lineFormat
                    .replace("{position}", index.toString())
                    .replace("{faction}", item.faction)
                    .replace("{wins}", item.wins.toString())
            }
            lines.add(ColorUtil.colorize(line))
        }

        return lines
    }

    private fun upsertDecentHologram(location: Location, lines: List<String>) {
        val dhApiClass = runCatching { Class.forName("eu.decentsoftware.holograms.api.DHAPI") }.getOrNull()
        if (dhApiClass == null) {
            if (!warnedMissingApi) {
                warnedMissingApi = true
                plugin.logger.warning("[FKoth] DHAPI class not found. Is DecentHolograms API available?")
            }
            return
        }

        val hologramId = plugin.config.getString(ConfigKeys.TOP_HOLOGRAM_ID, "fkoth_top5") ?: "fkoth_top5"

        val getMethod = dhApiClass.methods.firstOrNull { it.name == "getHologram" && it.parameterCount == 1 }
        val setLinesMethod = dhApiClass.methods.firstOrNull { it.name == "setHologramLines" && it.parameterCount == 2 }
        val createMethods = dhApiClass.methods.filter { it.name == "createHologram" }

        var hologram: Any? = runCatching { getMethod?.invoke(null, hologramId) }.getOrNull()

        if (hologram == null) {
            val createWithLines = createMethods.firstOrNull {
                it.parameterCount == 3 &&
                    it.parameterTypes[0] == String::class.java &&
                    Location::class.java.isAssignableFrom(it.parameterTypes[1]) &&
                    java.util.List::class.java.isAssignableFrom(it.parameterTypes[2])
            }

            val createWithPersistent = createMethods.firstOrNull {
                it.parameterCount == 4 &&
                    it.parameterTypes[0] == String::class.java &&
                    Location::class.java.isAssignableFrom(it.parameterTypes[1]) &&
                    (it.parameterTypes[2] == Boolean::class.java || it.parameterTypes[2] == java.lang.Boolean.TYPE) &&
                    java.util.List::class.java.isAssignableFrom(it.parameterTypes[3])
            }

            val createSimple = createMethods.firstOrNull {
                it.parameterCount == 2 &&
                    it.parameterTypes[0] == String::class.java &&
                    Location::class.java.isAssignableFrom(it.parameterTypes[1])
            }

            hologram = when {
                createWithLines != null -> runCatching { createWithLines.invoke(null, hologramId, location, lines) }.getOrNull()
                createWithPersistent != null -> runCatching { createWithPersistent.invoke(null, hologramId, location, false, lines) }.getOrNull()
                createSimple != null -> runCatching { createSimple.invoke(null, hologramId, location) }.getOrNull()
                else -> null
            }

            if (hologram == null) {
                hologram = runCatching { getMethod?.invoke(null, hologramId) }.getOrNull()
            }
        }

        if (hologram != null && setLinesMethod != null) {
            runCatching {
                setLinesMethod.invoke(null, hologram, lines)
            }.onFailure {
                plugin.logger.warning("[FKoth] Could not update DecentHolograms lines: ${it.message}")
            }
        }
    }
}
