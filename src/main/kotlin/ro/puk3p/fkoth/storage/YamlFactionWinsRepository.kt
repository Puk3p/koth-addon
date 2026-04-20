package ro.puk3p.fkoth.storage

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import ro.puk3p.fkoth.model.FactionWins
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
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
        val winsSection = yaml.createSection("wins")
        for ((faction, wins) in winsByFaction) {
            val key = encodeKey(faction)
            val entry = winsSection.createSection(key)
            entry.set("name", faction)
            entry.set("amount", wins)
        }
        yaml.save(file)
    }

    private fun load() {
        winsByFaction.clear()
        val yaml = YamlConfiguration.loadConfiguration(file)
        val winsSection = yaml.getConfigurationSection("wins")
        if (winsSection != null) {
            for (key in winsSection.getKeys(false)) {
                val entry = winsSection.getConfigurationSection(key) ?: continue
                val factionName = entry.getString("name")?.trim().orEmpty()
                if (factionName.isEmpty()) {
                    continue
                }
                winsByFaction[factionName] = entry.getInt("amount", 0).coerceAtLeast(0)
            }
            return
        }

        // Legacy migration support: old versions stored faction names directly as YAML paths.
        // This recursively rebuilds names such as "Team.One" from nested YAML sections.
        loadLegacySection(yaml, "")
    }

    private fun loadLegacySection(
        section: YamlConfiguration,
        prefix: String,
    ) {
        for (key in section.getKeys(false)) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            if (section.isInt(key)) {
                winsByFaction[fullKey] = section.getInt(key, 0).coerceAtLeast(0)
                continue
            }

            val child = section.getConfigurationSection(key) ?: continue
            loadLegacySection(child, fullKey)
        }
    }

    private fun loadLegacySection(
        section: org.bukkit.configuration.ConfigurationSection,
        prefix: String,
    ) {
        for (key in section.getKeys(false)) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            if (section.isInt(key)) {
                winsByFaction[fullKey] = section.getInt(key, 0).coerceAtLeast(0)
                continue
            }

            val child = section.getConfigurationSection(key) ?: continue
            loadLegacySection(child, fullKey)
        }
    }

    private fun resolveExistingKey(faction: String): String? {
        val needle = faction.lowercase(Locale.ROOT)
        return winsByFaction.keys.firstOrNull { it.lowercase(Locale.ROOT) == needle }
    }

    private fun encodeKey(raw: String): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
    }
}
