package me.bteuk.network.staff.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.Getter;
import me.bteuk.network.Network;
import me.bteuk.network.gui.regions.RegionInfo;
import me.bteuk.network.staff.gui.ModerationActionGui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ModerationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.Objects;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;

public class ModerationReasonListener implements Listener {

    private final NetworkUser u;
    private final ModerationActionGui gui;

    @Getter
    private final BukkitTask task;

    public ModerationReasonListener(NetworkUser u, ModerationActionGui gui) {

        this.u = u;
        this.gui = gui;

        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());

        //Start timer to automatically close the listener.
        task = Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
            //Send message to player telling them it's been timer out.
            if (u.player != null) {
                u.player.sendMessage(Utils.error("'Set " + gui.getType().label.toLowerCase(Locale.ROOT) + " reason' cancelled."));
            }
            unregister();
        }, 1200L);

    }

    @EventHandler
    public void ChatEvent(AsyncChatEvent e) {

        //Check if this is the correct player.
        if (e.getPlayer().equals(u.player)) {

            e.setCancelled(true);

            //Check if message is 256 characters or less.
            if (PlainTextComponentSerializer.plainText().serialize(e.message()).length() > 64) {

                e.getPlayer().sendMessage(Utils.error("The region tag can't be longer than 256 characters, please try again."));

            } else {

                //Set the reason.
                gui.setReason(PlainTextComponentSerializer.plainText().serialize(e.message()));
                e.getPlayer().sendMessage(Utils.success("Set reason to: ")
                        .append(e.message().color(DARK_AQUA)));

                //Refresh and reopen the gui.
                //This also cancels the task and unregisters the listener.
                gui.refresh();

                Bukkit.getScheduler().runTask(Network.getInstance(), () -> gui.open(u));

            }
        }
    }

    public void unregister() {
        AsyncChatEvent.getHandlerList().unregister(this);
    }
}
