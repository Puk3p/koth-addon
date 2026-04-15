package ro.puk3p.fkoth.storage

import ro.puk3p.fkoth.model.FactionWins

interface FactionWinsRepository {
    fun getWins(faction: String): Int
    fun addWins(faction: String, amount: Int): Int
    fun removeWins(faction: String, amount: Int): Int
    fun setWins(faction: String, amount: Int): Int
    fun removeFaction(faction: String)
    fun top(limit: Int): List<FactionWins>
    fun save()
}
