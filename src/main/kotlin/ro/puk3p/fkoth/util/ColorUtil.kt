package ro.puk3p.fkoth.util

import org.bukkit.ChatColor

object ColorUtil {
    fun colorize(text: String): String = ChatColor.translateAlternateColorCodes('&', text)
}
