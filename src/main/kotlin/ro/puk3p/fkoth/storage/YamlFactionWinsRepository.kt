package ro.puk3p.fkoth.storage

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import ro.puk3p.fkoth.model.FactionWins
import java.io.File
import java.util.Locale

class YamlFactionWinsRepository(
    private val plugin: JavaPlugin,
    private val fileName: String,
) : FactionWinsRepository {
    private val file = File(plugin.dataFolder, fileName)
    private val winsByFaction = linkedMapOf<String, Int>()

    init {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        load()
    }

    override fun getWins(faction: String): Int {
        val key = resolveExistingKey(faction) ?: faction
        return winsByFaction[key] ?: 0
    }

    override fun addWins(
        faction: String,
        amount: Int,
    ): Int {
        val key = resolveExistingKey(faction) ?: faction
        val newValue = (winsByFaction[key] ?: 0) + amount
        winsByFaction[key] = newValue.coerceAtLeast(0)
        save()
        return winsByFaction[key] ?: 0
    }

    override fun removeWins(
        faction: String,
        amount: Int,
    ): Int {
        return addWins(faction, -amount)
    }

    override fun setWins(
        faction: String,
        amount: Int,
    ): Int {
        val key = resolveExistingKey(faction) ?: faction
        winsByFaction[key] = amount.coerceAtLeast(0)
        save()
        return winsByFaction[key] ?: 0
    }

    override fun removeFaction(faction: String) {
        val key = resolveExistingKey(faction) ?: return
        winsByFaction.remove(key)
        save()
    }

    override fun top(limit: Int): List<FactionWins> {
        return winsByFaction
            .entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { FactionWins(it.key, it.value) }
    }

    override fun save() {
        val yaml = YamlConfiguration()
        for ((faction, wins) in winsByFaction) {
            yaml.set(faction, wins)
        }
        yaml.save(file)
    }

    private fun load() {
        val yaml = YamlConfiguration.loadConfiguration(file)
        for (key in yaml.getKeys(false)) {
            winsByFaction[key] = yaml.getInt(key, 0)
        }
    }

    private fun resolveExistingKey(faction: String): String? {
        val needle = faction.lowercase(Locale.ROOT)
        return winsByFaction.keys.firstOrNull { it.lowercase(Locale.ROOT) == needle }
    }
}
