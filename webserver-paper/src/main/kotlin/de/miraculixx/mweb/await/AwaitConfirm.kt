package de.miraculixx.mweb.await

import de.miraculixx.mvanilla.messages.*
import de.miraculixx.mweb.gui.logic.GUIEvent
import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.InventoryManager
import de.miraculixx.mweb.api.data.Head64
import de.miraculixx.mweb.gui.logic.items.ItemProvider
import de.miraculixx.mweb.gui.logic.items.skullTexture
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.unregister
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class AwaitConfirm(source: Player, onConfirm: () -> Unit, onCancel: () -> Unit) {
    private val gui = InventoryManager.inventoryBuilder("${source.uniqueId}-CONFIRM") {
        title = cmp("• ") + cmp(msgString("common.confirm"), NamedTextColor.DARK_GREEN)
        size = 3
        player = source
        itemProvider = InternalItemProvider()
        clickAction = InternalClickProvider(source, onConfirm, onCancel, this@AwaitConfirm).run
    }

    private val onClose = listen<InventoryCloseEvent> {
        if (it.inventory != gui.get()) return@listen
        if (it.reason != InventoryCloseEvent.Reason.PLAYER) return@listen
        disable()
        taskRunLater(1) {
            onCancel.invoke()
            it.player.soundError()
        }
    }

    private fun disable() {
        onClose.unregister()
    }

    private class InternalItemProvider : ItemProvider {
        override fun getSlotMap(): Map<Int, ItemStack> {
            return mapOf(
                12 to itemStack(Material.PLAYER_HEAD) {
                    meta {
                        customModel = 1
                        name = cmp(msgString("common.confirm"), cSuccess)
                    }
                    itemMeta = (itemMeta as SkullMeta).skullTexture(Head64.CHECKMARK_GREEN.value)
                },
                14 to itemStack(Material.PLAYER_HEAD) {
                    meta {
                        customModel = 2
                        name = cmp(msgString("common.cancel"), cError)
                    }
                    itemMeta = (itemMeta as SkullMeta).skullTexture(Head64.X_RED.value)
                }
            )
        }
    }

    private class InternalClickProvider(player: Player, onConfirm: () -> Unit, onCancel: () -> Unit, confirmer: AwaitConfirm) : GUIEvent {
        override val run: (InventoryClickEvent, CustomInventory) -> Unit = event@{ it: InventoryClickEvent, _: CustomInventory ->
            it.isCancelled = true
            if (it.whoClicked != player) return@event

            when (it.currentItem?.itemMeta?.customModel) {
                1 -> {
                    player.closeInventory()
                    onConfirm.invoke()
                }

                2 -> {
                    player.closeInventory()
                    onCancel.invoke()
                }

                else -> return@event
            }
            confirmer.disable()
        }
    }
}