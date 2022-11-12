package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ll implements CommandExecutor {

    private final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be run by a player."));
            return true;

        }

        //Get coordinates at the location of the player if they're in a region.
        NetworkUser u = Network.getInstance().getUser(p);

        if (u.inRegion) {

            try {

                double[] coords = bteGeneratorSettings.projection().toGeo(p.getLocation().getX() + u.dx, p.getLocation().getZ() + u.dz);

                p.sendMessage(Utils.chat("&aYour coordinates are &4" + coords[1] + "," + coords[0]));
                TextComponent message = new TextComponent(Utils.chat("&aClick here to view the coordinates in Google Maps."));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.google.com/maps/@?api=1&map_action=map&basemap=satellite&zoom=21&center=" + coords[1] + "," + coords[0]));
                p.spigot().sendMessage(message);
                return true;

            } catch (OutOfProjectionBoundsException e) {

                p.sendMessage(Utils.chat("&cYou must be standing in a region to get the coordinates."));
                return true;

            }

        } else {

            p.sendMessage(Utils.chat("&cYou must be standing in a region to get the coordinates."));
            return true;

        }
    }
}
