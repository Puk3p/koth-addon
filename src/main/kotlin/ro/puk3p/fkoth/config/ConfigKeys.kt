package ro.puk3p.fkoth.config

object ConfigKeys {
    const val STORAGE_FILE = "storage.file"
    const val RULE_IGNORE_NO_FACTION_WINNER = "rules.ignore-no-faction-winner"
    const val RULE_DELETE_ON_DISBAND = "rules.delete-wins-on-faction-disband"
    const val RULE_ALLOW_OFFLINE_PLAYER_LOOKUP = "rules.allow-offline-player-lookup"
    const val TOP_LIST_SIZE = "top.list-size"

    const val PAPI_ENABLED = "placeholderapi.enabled"

    const val KOTH_ENABLED = "integration.koth.enabled"
    const val KOTH_PLUGIN_NAME = "integration.koth.plugin-name"
    const val KOTH_END_EVENT_CLASS = "integration.koth.end-event-class"
    const val KOTH_WINNER_PATHS = "integration.koth.winner-paths"

    const val FACTIONS_DISBAND_EVENT_CLASS = "integration.factions.disband-event-class"
}
