@file:Suppress("UNCHECKED_CAST")

package de.miraculixx.mweb.commands

import de.miraculixx.mvanilla.commands.WhitelistHandling
import de.miraculixx.mvanilla.data.*
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mvanilla.serializer.enumOf
import de.miraculixx.mvanilla.web.WebServer
import de.miraculixx.mweb.gui.actions.ActionEmpty
import de.miraculixx.mweb.gui.actions.ActionFilesManage
import de.miraculixx.mweb.gui.actions.ActionFilesWhitelist
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.items.ItemLoading
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LiteralArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import net.axay.kspigot.extensions.console
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.audience.Audience
import org.apache.commons.codec.digest.DigestUtils
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class MainCommand : WhitelistHandling {
    val command = commandTree("webserver") {
        withAliases("ws")
        playerExecutor { player, _ ->
            player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
            GUITypes.FILE_MANAGE.buildInventory(player, "${player.uniqueId}-MANAGE", ItemFilesManage(File("./"), GUITypes.FILE_MANAGE), ActionFilesManage())
        }

        argument(LiteralArgument("whitelist").withPermission("webserver.whitelist")) {
            playerExecutor { player, _ ->
                player.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f)
                GUITypes.FILE_WHITELISTING.buildInventory(player, "${player.uniqueId}-WHITELIST", ItemFilesManage(File("./"), GUITypes.FILE_WHITELISTING), ActionFilesWhitelist())
            }
            literalArgument("add") {
                stringArgument("file") {
                    argument(StringArgument("access").replaceSuggestions(ArgumentSuggestions.strings(WhitelistType.values().map { it.name }))) {
                        anyExecutor { sender, args ->
                            val path = args[0] as String
                            val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                            sender.whitelistFile(path, access)
                        }
                        stringArgument("restriction") {
                            anyExecutor { sender, args ->
                                val path = args[0] as String
                                val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                                val restriction = args[2] as String
                                sender.whitelistFile(path, access, restriction)
                            }
                            stringArgument("timeout") {
                                anyExecutor { sender, args ->
                                    val path = args[0] as String
                                    val access = enumOf<WhitelistType>(args[1] as String) ?: WhitelistType.GLOBAL
                                    val restriction = args[2] as String
                                    val timeout = Duration.parse(args[3] as String)
                                    sender.whitelistFile(path, access, restriction, timeout)
                                }
                            }
                        }
                    }
                }
            }

            literalArgument("remove") {
                argument(StringArgument("file").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(ServerData.getWhitelists().map { StringTooltip.ofString(it.key, it.value.path) }))) {
                    anyExecutor { sender, args ->
                        val id = args[0] as String
                        sender.removeWhitelist(id)
                    }
                }
            }
        }

        argument(LiteralArgument("texturepack").withPermission("webserver.texturepack")) {
            textArgument("path") {
                entitySelectorArgumentManyPlayers("target") {
                    anyExecutor { sender, args ->
                        val path = args[0] as String
                        val targets = args[1] as List<Player>
                        sender.loadTP(path, targets, false)
                    }
                    booleanArgument("force") {
                        anyExecutor { sender, args ->
                            val path = args[0] as String
                            val targets = args[1] as List<Player>
                            val force = args[2] as Boolean
                            sender.loadTP(path, targets, force)
                        }
                    }
                }
            }
        }

        literalArgument("test") {
            playerExecutor { player, _ ->
                GUITypes.LOADING.buildInventory(player, "LOADING", ItemLoading(), ActionEmpty())
            }
        }
    }

    private fun Audience.loadTP(path: String, targets: List<Player>, force: Boolean) {
        val whitelist = Audience.empty().whitelistFile(path, WhitelistType.GLOBAL)
        if (whitelist == null) {
            sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(path)), cError))
            return
        }
        taskRunLater(20 * 60) {
            console.removeWhitelist(whitelist.first)
        }
        val file = File(whitelist.second.zippedTo ?: whitelist.second.path)
        val hash = DigestUtils.getSha1Digest().digest(file.readBytes())

        val prompt = msg("event.texturepackPrompt", listOf(file.name))
        val link = ServerData.getLink(whitelist.first)
        targets.forEach { player ->
            player.setResourcePack(link, hash, prompt, force)
        }
    }
}