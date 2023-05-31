package de.miraculixx.mweb.await

import de.miraculixx.mvanilla.data.plainSerializer
import de.miraculixx.mvanilla.data.prefix
import de.miraculixx.mvanilla.messages.*
import io.papermc.paper.event.player.AsyncChatEvent
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.unregister
import net.axay.kspigot.runnables.sync
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration

class AwaitChatMessage(
    sync: Boolean,
    private val player: Player,
    name: String,
    maxSeconds: Int,
    before: String?,
    private val advancedMode: Boolean,
    initMessage: Component,
    onChat: (String) -> Unit,
    private val callback: () -> Unit
) {
    private var counter = maxSeconds

    private val onChat = listen<AsyncChatEvent> {
        if (it.player != player) return@listen
        it.isCancelled = true
        val message = plainSerializer.serialize(it.message())
        val final = if (advancedMode) {
             when (message) {
                "#exit" -> before ?: ""
                "#clear" -> ""
                else -> message.replace('_', ' ')
            }
        } else message
        if (sync) sync { onChat.invoke(final) }
        else onChat.invoke(final)
        stop()
    }

    private val scheduler = task(sync, 0, 20) run@{
        if (counter <= 0) {
            stop()
            return@run
        }
        player.showTitle(Title.title(cmp("Enter $name", cHighlight), cmp("${counter}s"), Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ZERO)))
        sync {
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 99999, 1, false, false, false))
        }
        counter--
    }

    private fun stop() {
        scheduler?.cancel()
        onChat.unregister()
        player.showTitle(Title.title(emptyComponent(), emptyComponent()))
        sync {
            player.removePotionEffect(PotionEffectType.BLINDNESS)
            callback.invoke()
        }
    }

    init {
        player.closeInventory()
        player.sendMessage(initMessage)
        if (advancedMode && before != null) {
            val realBefore = before.replace(' ', '_')
            player.sendMessage(
                cmp(" ├> ") + (cmp(realBefore) + cmp(" (copy)", cMark)).addHover(msg("event.clickToCopy", listOf(realBefore)))
                .clickEvent(ClickEvent.suggestCommand(realBefore)))
        }
    }
}