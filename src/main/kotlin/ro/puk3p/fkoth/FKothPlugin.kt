package ro.puk3p.fkoth

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import ro.puk3p.fkoth.api.FKothApi
import ro.puk3p.fkoth.api.FKothApiImpl
import ro.puk3p.fkoth.command.FkothCommand
import ro.puk3p.fkoth.config.ConfigKeys
import ro.puk3p.fkoth.config.MessageKeys
import ro.puk3p.fkoth.integration.factions.FactionsUUIDAdapter
import ro.puk3p.fkoth.integration.hologram.TopHologramHook
import ro.puk3p.fkoth.integration.koth.KothHookListener
import ro.puk3p.fkoth.integration.placeholder.FKothPlaceholderExpansion
import ro.puk3p.fkoth.listener.FactionDisbandListener
import ro.puk3p.fkoth.service.FkothRules
import ro.puk3p.fkoth.service.FkothService
import ro.puk3p.fkoth.storage.YamlFactionWinsRepository
import ro.puk3p.fkoth.util.ColorUtil
import java.io.File

class FKothPlugin : JavaPlugin() {
    lateinit var service: FkothService
        private set

    private lateinit var messages: YamlConfiguration
    private var topHologramHook: TopHologramHook? = null
    private var kothHookEnabled: Boolean = false
    private var disbandHookEnabled: Boolean = false
    private var placeholderEnabled: Boolean = false
    private var hologramEnabled: Boolean = false

    override fun onEnable() {
        saveDefaultConfig()
        saveResourceIfMissing("messages.yml")
        saveResourceIfMissing("faction-wins.yml")
        loadMessages()

        val storageFile = config.getString(ConfigKeys.STORAGE_FILE, "faction-wins.yml") ?: "faction-wins.yml"
        val repository = YamlFactionWinsRepository(this, storageFile)
        val factionsAdapter = FactionsUUIDAdapter(this)
        val rules =
            FkothRules(
                ignoreNoFactionWinner = config.getBoolean(ConfigKeys.RULE_IGNORE_NO_FACTION_WINNER, true),
                allowOfflinePlayerLookup = config.getBoolean(ConfigKeys.RULE_ALLOW_OFFLINE_PLAYER_LOOKUP, true),
            )
        service = FkothService(repository, factionsAdapter, rules)

        registerPublicApi()

        val fkothCommand = FkothCommand(this)
        getCommand("fkoth")?.setExecutor(fkothCommand)
        getCommand("fkoth")?.tabCompleter = fkothCommand

        registerKothHook()
        registerFactionDisbandHook()
        registerPlaceholderExpansion()
        registerTopHologramHook()

        logger.info("FKoth enabled.")
    }

    override fun onDisable() {
        topHologramHook?.stop()
        service.save()
        server.servicesManager.unregisterAll(this)
    }

    fun reloadPlugin() {
        reloadConfig()
        loadMessages()
        val rules =
            FkothRules(
                ignoreNoFactionWinner = config.getBoolean(ConfigKeys.RULE_IGNORE_NO_FACTION_WINNER, true),
                allowOfflinePlayerLookup = config.getBoolean(ConfigKeys.RULE_ALLOW_OFFLINE_PLAYER_LOOKUP, true),
            )
        service.updateRules(rules)

        topHologramHook?.stop()
        registerTopHologramHook()
    }

    fun debugStatus(): List<Pair<String, String>> {
        return listOf(
            "KoTH Hook" to if (kothHookEnabled) "ENABLED" else "DISABLED",
            "Factions Disband Hook" to if (disbandHookEnabled) "ENABLED" else "DISABLED",
            "PlaceholderAPI" to if (placeholderEnabled) "ENABLED" else "DISABLED",
            "Top Hologram" to if (hologramEnabled) "ENABLED" else "DISABLED",
            "Tracked Factions" to service.getTrackedFactionCount().toString(),
        )
    }

    fun message(
        key: String,
        placeholders: Map<String, String> = emptyMap(),
    ): String {
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

    private fun registerPublicApi() {
        val api = FKothApiImpl(service)
        server.servicesManager.register(FKothApi::class.java, api, this, ServicePriority.Normal)
        logger.info("[FKoth] Public API registered in ServicesManager.")
    }

    private fun registerPlaceholderExpansion() {
        val papiEnabled = config.getBoolean(ConfigKeys.PAPI_ENABLED, true)
        if (!papiEnabled) {
            placeholderEnabled = false
            return
        }

        val papiPlugin = server.pluginManager.getPlugin("PlaceholderAPI")
        if (papiPlugin == null) {
            placeholderEnabled = false
            return
        }

        FKothPlaceholderExpansion(service).register()
        placeholderEnabled = true
        logger.info("[FKoth] PlaceholderAPI expansion registered.")
    }

    private fun registerKothHook() {
        val enabled = config.getBoolean(ConfigKeys.KOTH_ENABLED, true)
        if (!enabled) {
            kothHookEnabled = false
            return
        }

        val pluginName = config.getString(ConfigKeys.KOTH_PLUGIN_NAME, "KoTH") ?: "KoTH"
        val endEventClass =
            config.getString(
                ConfigKeys.KOTH_END_EVENT_CLASS,
                "subside.plugins.koth.events.KothEndEvent",
            ) ?: "subside.plugins.koth.events.KothEndEvent"

        val paths =
            config.getStringList(ConfigKeys.KOTH_WINNER_PATHS).ifEmpty {
                listOf(
                    "getWinner",
                    "getCappingPlayer",
                    "getRunningKoth.getWinner",
                    "getRunningKoth.getCappingPlayer",
                )
            }

        val hook = KothHookListener(this, service, pluginName, endEventClass, paths)
        if (hook.register()) {
            kothHookEnabled = true
            logger.info("[FKoth] KoTH hook enabled.")
        } else {
            kothHookEnabled = false
            logger.warning("[FKoth] KoTH hook not enabled. Plugin/event not found.")
        }
    }

    private fun registerTopHologramHook() {
        val hook = TopHologramHook(this, service)
        hologramEnabled = hook.start()
        topHologramHook = hook
    }

    private fun registerFactionDisbandHook() {
        val enabled = config.getBoolean(ConfigKeys.RULE_DELETE_ON_DISBAND, true)
        if (!enabled) {
            disbandHookEnabled = false
            return
        }

        val eventClass =
            config.getString(
                ConfigKeys.FACTIONS_DISBAND_EVENT_CLASS,
                "com.massivecraft.factions.event.FactionDisbandEvent",
            ) ?: "com.massivecraft.factions.event.FactionDisbandEvent"

        val hook = FactionDisbandListener(this, service, eventClass)
        if (hook.register()) {
            disbandHookEnabled = true
            logger.info("[FKoth] Factions disband cleanup hook enabled.")
        } else {
            disbandHookEnabled = false
        }
    }

    private fun saveResourceIfMissing(name: String) {
        val file = File(dataFolder, name)
        if (!file.exists()) {
            saveResource(name, false)
        }
    }
}
