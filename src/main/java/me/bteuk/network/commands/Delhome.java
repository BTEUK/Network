package me.bteuk.network.commands;

import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Delhome implements CommandExecutor {

    private final GlobalSQL globalSQL;

    public Delhome(GlobalSQL globalSQL) {
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

            //Check if the default home exists.
            if (!globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name IS NULL;")) {
                p.sendMessage(Utils.error("You not have a default home set, set one with &4/sethome"));
                return true;
            }

            //Get coordinate ID.
            int coordinate_id = globalSQL.getInt("SELECT coordinate_id FROM home WHERE uuid='" + p.getUniqueId() + "' AND name IS NULL;");

            //Delete default home.
            globalSQL.update("DELETE FROM home WHERE uuid='" + p.getUniqueId() + "' AND name IS NULL;");
            p.sendMessage(Utils.success("Default home removed."));

            //Delete coordinate id.
            globalSQL.update("DELETE FROM coordinates WHERE id=" + coordinate_id + ";");

        } else {

            //Check for permission.
            if (!p.hasPermission("uknet.navigation.homes")) {
                p.sendMessage(Utils.error("You do not have permission to delete multiple homes, only to delete a default home using &4/delhome"));
                return true;
            }

            //Get the name.
            String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

            //Get coordinate ID.
            int coordinate_id = globalSQL.getInt("SELECT coordinate_id FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';");

            //Check is home with this name exists.
            if (!globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';")) {
                p.sendMessage(Utils.error("You do not have a home set with the name &4" + name + "&c."));
                return true;
            }

            //Delete home
            globalSQL.update("DELETE FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';");
            p.sendMessage(Utils.success("&3" + name + " &ahome removed."));

            //Delete coordinate id.
            globalSQL.update("DELETE FROM coordinates WHERE id=" + coordinate_id + ";");

        }

        return true;

    }
}
