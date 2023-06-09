package de.miraculixx.mweb.gui.logic.event

import de.miraculixx.mweb.gui.logic.data.CustomInventory
import de.miraculixx.mweb.gui.logic.data.GUIClick
import de.miraculixx.mweb.gui.logic.event.GUIClickEvent
import de.miraculixx.mweb.gui.logic.event.GUICloseEvent
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType

//TODO -> Silk Licence

class GUIEventHandler(
    type: MenuType<*>,
    syncId: Int,
    playerInv: Inventory,
    private val customInv: CustomInventory,
    height: Int,
    private val clickEvent: ((GUIClickEvent, CustomInventory) -> Unit)?,
    private val closeEvent: ((GUICloseEvent, CustomInventory) -> Unit)?,
    private val defaultClickAction: ((GUIClickEvent, CustomInventory) -> Unit)?
): ChestMenu(type, syncId, playerInv, customInv, height) {
    override fun clicked(slot: Int, click: Int, clickType: ClickType, player: Player) {
        val event = GUIClickEvent(customInv, player, slot, container.getItem(slot), GUIClick.fromSlotActionType(clickType, click), click)
        defaultClickAction?.invoke(event, customInv)
        clickEvent?.invoke(event, customInv)
        if (event.isCancelled) sendAllDataToRemote()
        else super.clicked(slot, click, clickType, player)
    }

    override fun removed(player: Player) {
        val event = GUICloseEvent(customInv, player)
        closeEvent?.invoke(event, customInv)
        customInv.stopOpen(player)
    }
}