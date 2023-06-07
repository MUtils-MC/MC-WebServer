package de.miraculixx.mweb.gui.actions

import de.miraculixx.mvanilla.commands.WhitelistHandling
import de.miraculixx.mvanilla.data.GUITypes
import de.miraculixx.mvanilla.data.ServerData
import de.miraculixx.mvanilla.data.WhitelistType
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.buildInventory
import de.miraculixx.mweb.gui.items.ItemCreateWhitelist
import de.miraculixx.mweb.gui.items.ItemFilesManage
import de.miraculixx.mweb.gui.items.ItemLoading
import de.miraculixx.mweb.gui.items.ItemWhitelists
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.InventoryUtils.get
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.module.permVisual
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.axay.kspigot.items.customModel
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class ActionFilesWhitelist: GUIEvent, WhitelistHandling, ActionFiles {
    override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, inv: CustomInventory ->
        it.isCancelled = true
        val player = it.whoClicked as? Player ?: return@event
        val item = it.currentItem ?: return@event
        val meta = item.itemMeta ?: return@event
        val provider = inv.itemProvider as? ItemFilesManage ?: return@event

        when (meta.customModel) {
            100 -> {
                val path = meta.persistentDataContainer.get(provider.pathNamespace) ?: provider.currentFolder.path
                val file = File(path)
                if (!file.exists()) {
                    player.soundError()
                    inv.update() //Refresh files
                    player.sendMessage(prefix + cmp(msgString("event.fileNotFound", listOf(file.path)), cError))
                }

                when (it.click) {
                    //Navigate into file (folder only)
                    ClickType.LEFT -> file.navigate(player, provider, inv)

                    ClickType.NUMBER_KEY -> {
                        when (it.hotbarButton) {
                            //Create global link
                            0 -> {
                                if (!player.permVisual("mweb.whitelist.global")) return@event
                                player.whitelistFile(path, WhitelistType.GLOBAL)
                                player.soundEnable()
                                inv.update()
                            }
                            //Create private link for the user
                            1 -> {
                                if (!player.permVisual("mweb.whitelist.privat")) return@event
                                if (file.isDirectory) {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        GUITypes.LOADING.buildInventory(player, "LOADING", ItemLoading(), ActionEmpty())
                                        player.whitelistFile(path, WhitelistType.USER_RESTRICTED, player.uniqueId.toString())
                                        inv.update()
                                        inv.open(player)
                                    }
                                } else {
                                    player.whitelistFile(path, WhitelistType.USER_RESTRICTED, player.uniqueId.toString())
                                    inv.update()
                                }
                                player.soundEnable()
                            }
                            //Open custom link creator
                            2 -> {
                                if (!player.permVisual("mweb.whitelist.custom")) return@event
                                GUITypes.CREATE_CUSTOM_WHITELIST.buildInventory(player, "${player.uniqueId}-CREATE_WHITELIST", ItemCreateWhitelist(path), ActionCreateWhitelist())
                            }
                            //Manage file links
                            3 -> {
                                if (!player.permVisual("mweb.whitelist.manage")) return@event
                                if (ServerData.getWhitelists(file.path).isEmpty()) {
                                    player.soundStone()
                                    return@event
                                }
                                player.click()
                                GUITypes.MANAGE_WHITELISTS.buildInventory(player, "${player.uniqueId}-MANAGE_WHITELIST", ItemWhitelists(file), ActionWhitelists(inv))
                            }

                            else -> player.soundStone()
                        }
                    }

                    else -> player.soundStone()
                }
            }

            99 -> provider.currentFolder.navBack(provider, player, inv)

            1 -> player.openManager(provider)
            2 -> player.soundStone()
            3 -> player.openUpload(provider)
        }
    }
}