package ro.puk3p.fkoth.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import ro.puk3p.fkoth.FKothPlugin
import ro.puk3p.fkoth.config.MessageKeys
import ro.puk3p.fkoth.service.AddByPlayerResult
import java.util.Locale

class FkothCommand(
    private val plugin: FKothPlugin
) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.message(MessageKeys.USAGE_MAIN))
            return true
        }

        when (args[0].lowercase(Locale.ROOT)) {
            "add" -> handleAdd(sender, args)
            "remove" -> handleRemove(sender, args)
            "set" -> handleSet(sender, args)
            "stats" -> handleStats(sender)
            "top" -> handleTop(sender)
            else -> sender.sendMessage(plugin.message(MessageKeys.USAGE_MAIN))
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (args.size == 1) {
            return mutableListOf("add", "remove", "set", "stats", "top")
                .filter { it.startsWith(args[0], ignoreCase = true) }
                .toMutableList()
        }
        return mutableListOf()
    }

    private fun handleAdd(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fkoth.admin")) {
            sender.sendMessage(plugin.message(MessageKeys.NO_PERMISSION))
            return
        }
        if (args.size < 3) {
            sender.sendMessage(plugin.message(MessageKeys.USAGE_ADD))
            return
        }

        val playerName = args[1]
        val amount = args[2].toIntOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage(plugin.message(MessageKeys.INVALID_NUMBER))
            return
        }

        val (status, faction) = plugin.service.addWinsForPlayer(playerName, amount)
        when (status) {
            AddByPlayerResult.SUCCESS -> sender.sendMessage(
                plugin.message(
                    MessageKeys.ADD_SUCCESS,
                    mapOf("{amount}" to amount.toString(), "{faction}" to (faction ?: "-"))
                )
            )
            AddByPlayerResult.PLAYER_OFFLINE -> sender.sendMessage(plugin.message(MessageKeys.PLAYER_OFFLINE))
            AddByPlayerResult.NO_FACTION -> sender.sendMessage(plugin.message(MessageKeys.IGNORED_NO_FACTION))
        }
    }

    private fun handleRemove(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fkoth.admin")) {
            sender.sendMessage(plugin.message(MessageKeys.NO_PERMISSION))
            return
        }
        if (args.size < 3) {
            sender.sendMessage(plugin.message(MessageKeys.USAGE_REMOVE))
            return
        }

        val faction = args[1]
        val amount = args[2].toIntOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage(plugin.message(MessageKeys.INVALID_NUMBER))
            return
        }

        plugin.service.removeWinsForFaction(faction, amount)
        sender.sendMessage(
            plugin.message(
                MessageKeys.REMOVE_SUCCESS,
                mapOf("{amount}" to amount.toString(), "{faction}" to faction)
            )
        )
    }

    private fun handleSet(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fkoth.admin")) {
            sender.sendMessage(plugin.message(MessageKeys.NO_PERMISSION))
            return
        }
        if (args.size < 3) {
            sender.sendMessage(plugin.message(MessageKeys.USAGE_SET))
            return
        }

        val faction = args[1]
        val amount = args[2].toIntOrNull()
        if (amount == null || amount < 0) {
            sender.sendMessage(plugin.message(MessageKeys.INVALID_NUMBER))
            return
        }

        plugin.service.setWinsForFaction(faction, amount)
        sender.sendMessage(
            plugin.message(
                MessageKeys.SET_SUCCESS,
                mapOf("{amount}" to amount.toString(), "{faction}" to faction)
            )
        )
    }

    private fun handleStats(sender: CommandSender) {
        if (!sender.hasPermission("fkoth.player")) {
            sender.sendMessage(plugin.message(MessageKeys.NO_PERMISSION))
            return
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.message(MessageKeys.STATS_ONLY_PLAYER))
            return
        }

        val faction = plugin.service.getFactionForPlayer(sender)
        if (faction == null) {
            sender.sendMessage(plugin.message(MessageKeys.NO_FACTION))
            return
        }

        val wins = plugin.service.getWinsForFaction(faction)
        sender.sendMessage(
            plugin.message(
                MessageKeys.STATS_SELF,
                mapOf("{faction}" to faction, "{wins}" to wins.toString())
            )
        )
    }

    private fun handleTop(sender: CommandSender) {
        if (!sender.hasPermission("fkoth.player")) {
            sender.sendMessage(plugin.message(MessageKeys.NO_PERMISSION))
            return
        }

        val topSize = plugin.config.getInt("top.list-size", 10)
        val top = plugin.service.top(topSize)

        sender.sendMessage(plugin.message(MessageKeys.TOP_HEADER, mapOf("{size}" to topSize.toString())))
        top.forEachIndexed { index, item ->
            sender.sendMessage(
                plugin.message(
                    MessageKeys.TOP_LINE,
                    mapOf(
                        "{position}" to (index + 1).toString(),
                        "{faction}" to item.faction,
                        "{wins}" to item.wins.toString()
                    )
                )
            )
        }
    }
}
