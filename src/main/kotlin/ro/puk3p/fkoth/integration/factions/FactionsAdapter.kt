package ro.puk3p.fkoth.integration.factions

import java.util.UUID

interface FactionsAdapter {
    fun isAvailable(): Boolean

    fun getFactionNameByPlayerName(playerName: String): String?

    fun getFactionNameByPlayerUuid(uuid: UUID): String?
}
