package me.bteuk.network.commands;

import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Sethome implements CommandExecutor {

    private GlobalSQL globalSQL;

    public Sethome(GlobalSQL globalSQL) {
        this.globalSQL = globalSQL;
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
                p.sendMessage(Utils.error("You already have a default home set, to delete it type &4/delhome"));
                return true;
            }

            //Set home to current location.
            int coordinate_id = globalSQL.addCoordinate(p.getLocation());

            globalSQL.update("INSERT INTO home(coordinate_id,uuid) VALUES(" + coordinate_id + ",'" + p.getUniqueId() + "');");

            p.sendMessage(Utils.success("Default home set to current location, to teleport here type &4/home"));

        } else {

            //Check for permission.
            if (!p.hasPermission("uknet.navigation.homes")) {
                p.sendMessage(Utils.error("You do not have permission to set multiple homes, only to set a default home using &4/sethome"));
                return true;
            }

            //Check if a home with this name already exists.
            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            //Check name length.
            if (name.length() > 64) {
                p.sendMessage(Utils.error("The home name must be 64 characters or less."));
                return true;
            }

            if (globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';")) {
                p.sendMessage(Utils.error("You already have a home set with the name &4" + name + "&c, you can delete it by typing &4/delhome " + name));
                return true;
            }

            //Set home to current location.
            int coordinate_id = globalSQL.addCoordinate(p.getLocation());

            globalSQL.update("INSERT INTO home(coordinate_id,uuid,name) VALUES(" + coordinate_id + ",'" + p.getUniqueId() + "','" + name + "');");

            p.sendMessage("Home set to current location, to teleport here type &4/home " + name);

        }

        return true;

    }
}
