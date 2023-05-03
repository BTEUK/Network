package me.bteuk.network.utils.regions;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.bteuk.network.Network;
import me.bteuk.network.gui.regions.RegionInfo;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public class RegionTagListener implements Listener {

    private final Player p;
    private final Region region;

    private final BukkitTask task;

    public RegionTagListener(Player p, Region region) {

        Bukkit.getServer().getPluginManager().registerEvents(this, Network.getInstance());

        this.p = p;
        this.region = region;

        //Start timer to automatically close the listener.
        task = Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
            //Send message to player telling them it's been timer out.
            if (p != null) {
                p.sendMessage(Utils.error("'Set Region Tag' cancelled."));
            }
            unregister();
        }, 1200L);

    }

    @EventHandler
    public void ChatEvent(AsyncChatEvent e) {

        //Check if this is the correct player.
        if (e.getPlayer().equals(p)) {

            e.setCancelled(true);

            //Check if message is under 64 character.
            if (PlainTextComponentSerializer.plainText().serialize(e.message()).length() > 64) {
                e.getPlayer().sendMessage(Utils.error("The region tag can't be longer than 64 characters."));
            } else {

                //Set region tag.
                region.setTag(p.getUniqueId().toString(), PlainTextComponentSerializer.plainText().serialize(e.message()));

                //Send message to player.
                p.sendMessage(Utils.success("Set tag for region ")
                        .append(Component.text(region.regionName(), NamedTextColor.DARK_AQUA))
                        .append(Utils.success(" to "))
                        .append(e.message().color(NamedTextColor.DARK_AQUA)));

                //Unregister listener and task.
                task.cancel();
                unregister();

                //Reset the regionInfo gui
                NetworkUser u = Network.getInstance().getUser(p);
                Objects.requireNonNull(u).mainGui.delete();
                u.mainGui = new RegionInfo(region, p.getUniqueId().toString());

            }
        }
    }

    public void unregister() {
        AsyncChatEvent.getHandlerList().unregister(this);
    }
}
