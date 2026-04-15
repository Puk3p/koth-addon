package ro.puk3p.fkoth

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import ro.puk3p.fkoth.command.FkothCommand
import ro.puk3p.fkoth.config.ConfigKeys
import ro.puk3p.fkoth.config.MessageKeys
import ro.puk3p.fkoth.integration.factions.FactionsUUIDAdapter
import ro.puk3p.fkoth.integration.koth.KothHookListener
import ro.puk3p.fkoth.integration.placeholder.FKothPlaceholderExpansion
import ro.puk3p.fkoth.listener.FactionDisbandListener
import ro.puk3p.fkoth.service.FkothService
import ro.puk3p.fkoth.storage.YamlFactionWinsRepository
import ro.puk3p.fkoth.util.ColorUtil
import java.io.File

class FKothPlugin : JavaPlugin() {

    lateinit var service: FkothService
        private set

    private lateinit var messages: YamlConfiguration

    override fun onEnable() {
        saveDefaultConfig()
        saveResourceIfMissing("messages.yml")
        saveResourceIfMissing("faction-wins.yml")
        loadMessages()

        val storageFile = config.getString(ConfigKeys.STORAGE_FILE, "faction-wins.yml") ?: "faction-wins.yml"
        val repository = YamlFactionWinsRepository(this, storageFile)
        val factionsAdapter = FactionsUUIDAdapter(this)
        service = FkothService(repository, factionsAdapter)

        val fkothCommand = FkothCommand(this)
        getCommand("fkoth")?.setExecutor(fkothCommand)
        getCommand("fkoth")?.tabCompleter = fkothCommand

        registerKothHook()
        registerFactionDisbandHook()
        registerPlaceholderExpansion()

        logger.info("FKoth enabled.")
    }

    override fun onDisable() {
        service.save()
    }

    fun message(key: String, placeholders: Map<String, String> = emptyMap()): String {
        var text = messages.getString(key, key) ?: key
        for ((from, to) in placeholders) {
            text = text.replace(from, to)
        }

        val prefix = messages.getString(MessageKeys.PREFIX, "") ?: ""
        return ColorUtil.colorize(prefix + text)
    }

    private fun loadMessages() {
        messages = YamlConfiguration.loadConfiguration(File(dataFolder, "messages.yml"))
    }

    private fun registerPlaceholderExpansion() {
        val papiEnabled = config.getBoolean(ConfigKeys.PAPI_ENABLED, true)
        if (!papiEnabled) {
            return
        }

        val papiPlugin = server.pluginManager.getPlugin("PlaceholderAPI")
        if (papiPlugin == null) {
            return
        }

        FKothPlaceholderExpansion(service).register()
        logger.info("[FKoth] PlaceholderAPI expansion registered.")
    }

    private fun registerKothHook() {
        val enabled = config.getBoolean(ConfigKeys.KOTH_ENABLED, true)
        if (!enabled) {
            return
        }

        val pluginName = config.getString(ConfigKeys.KOTH_PLUGIN_NAME, "KoTH") ?: "KoTH"
        val endEventClass = config.getString(
            ConfigKeys.KOTH_END_EVENT_CLASS,
            "subside.plugins.koth.events.KothEndEvent"
        ) ?: "subside.plugins.koth.events.KothEndEvent"

        val paths = config.getStringList(ConfigKeys.KOTH_WINNER_PATHS).ifEmpty {
            listOf(
                "getWinner",
                "getCappingPlayer",
                "getRunningKoth.getWinner",
                "getRunningKoth.getCappingPlayer"
            )
        }

        val hook = KothHookListener(this, service, pluginName, endEventClass, paths)
        if (hook.register()) {
            logger.info("[FKoth] KoTH hook enabled.")
        } else {
            logger.warning("[FKoth] KoTH hook not enabled. Plugin/event not found.")
        }
    }

    private fun registerFactionDisbandHook() {
        val enabled = config.getBoolean(ConfigKeys.RULE_DELETE_ON_DISBAND, true)
        if (!enabled) {
            return
        }

        val eventClass = config.getString(
            ConfigKeys.FACTIONS_DISBAND_EVENT_CLASS,
            "com.massivecraft.factions.event.FactionDisbandEvent"
        ) ?: "com.massivecraft.factions.event.FactionDisbandEvent"

        val hook = FactionDisbandListener(this, service, eventClass)
        if (hook.register()) {
            logger.info("[FKoth] Factions disband cleanup hook enabled.")
        }
    }

    private fun saveResourceIfMissing(name: String) {
        val file = File(dataFolder, name)
        if (!file.exists()) {
            saveResource(name, false)
        }
    }
}
