package ro.puk3p.fkoth.config

object ConfigKeys {
    const val STORAGE_FILE = "storage.file"
    const val RULE_IGNORE_NO_FACTION_WINNER = "rules.ignore-no-faction-winner"
    const val RULE_DELETE_ON_DISBAND = "rules.delete-wins-on-faction-disband"
    const val RULE_ALLOW_OFFLINE_PLAYER_LOOKUP = "rules.allow-offline-player-lookup"
    const val TOP_LIST_SIZE = "top.list-size"
    const val TOP_HOLOGRAM_ENABLED = "top.hologram.enabled"
    const val TOP_HOLOGRAM_ID = "top.hologram.id"
    const val TOP_HOLOGRAM_ENTRIES = "top.hologram.entries"
    const val TOP_HOLOGRAM_TITLE = "top.hologram.title"
    const val TOP_HOLOGRAM_LINE_FORMAT = "top.hologram.line-format"
    const val TOP_HOLOGRAM_EMPTY_LINE_FORMAT = "top.hologram.empty-line-format"
    const val TOP_HOLOGRAM_REFRESH_SECONDS = "top.hologram.refresh-seconds"
    const val TOP_HOLOGRAM_LOCATION_WORLD = "top.hologram.location.world"
    const val TOP_HOLOGRAM_LOCATION_X = "top.hologram.location.x"
    const val TOP_HOLOGRAM_LOCATION_Y = "top.hologram.location.y"
    const val TOP_HOLOGRAM_LOCATION_Z = "top.hologram.location.z"

    const val PAPI_ENABLED = "placeholderapi.enabled"

    const val KOTH_ENABLED = "integration.koth.enabled"
    const val KOTH_PLUGIN_NAME = "integration.koth.plugin-name"
    const val KOTH_END_EVENT_CLASS = "integration.koth.end-event-class"
    const val KOTH_WINNER_PATHS = "integration.koth.winner-paths"

    const val FACTIONS_DISBAND_EVENT_CLASS = "integration.factions.disband-event-class"
}
