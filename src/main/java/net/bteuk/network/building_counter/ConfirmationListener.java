package net.bteuk.network.building_counter;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.bteuk.network.commands.Buildings;
import net.bteuk.network.lib.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.util.Locale;

public class ConfirmationListener implements Listener {
    private Buildings instancefrom;

    public ConfirmationListener(Buildings b)
    {
        instancefrom = b;
    }


    @EventHandler
    public void chatEvent(AsyncChatEvent e)
    {
        if(e.getPlayer().getUniqueId() == instancefrom.player.getUniqueId()) {
            e.getHandlers().unregister(this);
            e.setCancelled(true);
            if (((net.kyori.adventure.text.TextComponent) e.message()).content().equals("y")) {
                instancefrom.addBuildingToDataBase(e.getPlayer());
            } else {
                e.getPlayer().sendMessage(ChatUtils.error("No building added"));
            }
        }
    }

}
