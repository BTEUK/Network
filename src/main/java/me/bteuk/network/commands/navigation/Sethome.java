package me.bteuk.network.commands.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Sethome implements CommandExecutor {

    private final GlobalSQL globalSQL;

    //Constructor to enable the command.
    public Sethome(Network instance, GlobalSQL globalSQL) {

        this.globalSQL = globalSQL;

        //Register command.
        PluginCommand command = instance.getCommand("sethome");

        if (command == null) {
            LOGGER.warning("Sethome command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Utils.error("This command can only be used by players."));
            return true;
        }

        //If no args set default home.
        //Else try to set homes with specific names.
        //For multiple homes the player needs permission.
        if (args.length == 0) {

            //If already has a default home set, the player first has to delete it.
            if (globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name IS NULL;")) {
                p.sendMessage(Utils.error("You already have a default home set, to delete it type ")
                        .append(Component.text("/delhome", NamedTextColor.DARK_RED)));
                return true;
            }

            //Set home to current location.
            int coordinate_id = getCoordinateID(p.getLocation());

            globalSQL.update("INSERT INTO home(coordinate_id,uuid) VALUES(" + coordinate_id + ",'" + p.getUniqueId() + "');");

            p.sendMessage(Utils.success("Default home set to current location, to teleport here type ")
                    .append(Component.text("/home", NamedTextColor.DARK_AQUA)));

        } else {

            //Check for permission.
            if (!p.hasPermission("uknet.navigation.homes")) {
                p.sendMessage(Utils.error("You do not have permission to set multiple homes, only to set a default home using ")
                        .append(Component.text("/sethome", NamedTextColor.DARK_RED)));
                return true;
            }

            //Check if a home with this name already exists.
            String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

            //Check name length.
            if (name.length() > 64) {
                p.sendMessage(Utils.error("The home name must be 64 characters or less."));
                return true;
            }

            if (globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';")) {
                p.sendMessage(Utils.error("You already have a home set with the name ")
                        .append(Component.text(name, NamedTextColor.DARK_RED))
                        .append(Utils.error(", you can delete it by typing "))
                        .append(Component.text("/delhome " + name, NamedTextColor.DARK_RED)));
                return true;
            }

            //Set home to current location.
            int coordinate_id = getCoordinateID(p.getLocation());

            globalSQL.update("INSERT INTO home(coordinate_id,uuid,name) VALUES(" + coordinate_id + ",'" + p.getUniqueId() + "','" + name + "');");

            p.sendMessage(Utils.success("Home set to current location, to teleport here type ")
                    .append(Component.text("/home " + name, NamedTextColor.DARK_AQUA)));

        }

        return true;

    }

    private int getCoordinateID(Location l) {
        //Create location coordinate.
        return Network.getInstance().globalSQL.addCoordinate(l);
    }
}
