package de.miraculixx.mweb.gui.logic.data

import net.minecraft.world.inventory.ClickType

enum class GUIClick {
    /**
     * A normal slot click.
     */
    LEFT_CLICK,
    /**
     * A double slot click (to pick up all items of a stack).
     */
    DOUBLE_LEFT_CLICK,
    /**
     * Inserting items into an inventory.
     */
    INSERT,
    /**
     * Moving items from one inventory to another
     * using shift.
     */
    SHIFT_LEFT_CLICK,
    SHIFT_RIGHT_CLICK,
    /**
     * Moving items from one inventory to another
     * using a hotkey (e.g. 0 - 9).
     */
    HOTKEY_SWAP,
    /**
     * **CREATIVE ONLY** Cloning an ItemStack by middle clicking it.
     */
    MIDDLE_CLICK,
    /**
     * Throw away a whole ItemStack (e.g. using CTRL + Q).
     */
    DROP_STRG,
    /**
     * Throw away one item of an ItemStack (e.g. using Q).
     */
    DROP,
    /**
     * Right click, drag items.
     */
    RIGHT_CLICK_HOLD,
    /**
     * Start using a dragging feature.
     */
    RIGHT_CLICK,
    /**
     * Finish using a dragging feature.
     */
    RIGHT_CLICK_HOLD_STOP;

    companion object {
        fun fromSlotActionType(
            slotActionType: ClickType,
            clickID: Int
        ): GUIClick {
            return when (slotActionType) {
                ClickType.PICKUP -> when (clickID) {
                    0 -> LEFT_CLICK
                    1 -> RIGHT_CLICK
                    else -> MIDDLE_CLICK
                }
                ClickType.PICKUP_ALL -> DOUBLE_LEFT_CLICK
                ClickType.QUICK_MOVE -> when (clickID) {
                    0 -> SHIFT_LEFT_CLICK
                    1 -> SHIFT_RIGHT_CLICK
                    else -> MIDDLE_CLICK
                }
                ClickType.SWAP -> HOTKEY_SWAP
                ClickType.CLONE -> MIDDLE_CLICK
                ClickType.THROW -> if (clickID == 1) DROP_STRG else DROP
                ClickType.QUICK_CRAFT -> when (clickID) {
                    0 -> RIGHT_CLICK
                    2 -> RIGHT_CLICK_HOLD_STOP
                    else -> RIGHT_CLICK_HOLD
                }
            }
        }
    }

    fun isLeftClick(): Boolean {
        return this == LEFT_CLICK || this == SHIFT_LEFT_CLICK
    }

    fun isRightClick(): Boolean {
        return this == RIGHT_CLICK || this == SHIFT_RIGHT_CLICK
    }

    fun isShiftClick(): Boolean {
        return this == SHIFT_RIGHT_CLICK || this == SHIFT_LEFT_CLICK
    }
}