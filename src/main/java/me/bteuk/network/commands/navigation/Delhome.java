package me.bteuk.network.commands.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Delhome implements CommandExecutor, TabCompleter {

    private final GlobalSQL globalSQL;

    //Constructor to enable the command.
    public Delhome(Network instance, GlobalSQL globalSQL) {

        this.globalSQL = globalSQL;

        //Register command.
        PluginCommand command = instance.getCommand("delhome");

        if (command == null) {
            LOGGER.warning("Delhome command not added to plugin.yml, it will therefore not be enabled.");
            return;
        }

        //Set executor.
        command.setExecutor(this);

        //Set tab completer.
        command.setTabCompleter(this);

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
                p.sendMessage(Utils.error("You not have a default home set, set one with ")
                        .append(Component.text("/sethome", NamedTextColor.DARK_RED)));
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
                p.sendMessage(Utils.error("You do not have permission to delete multiple homes, only to delete a default home using ")
                        .append(Component.text("/delhome", NamedTextColor.DARK_RED)));
                return true;
            }

            //Get the name.
            String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

            //Get coordinate ID.
            int coordinate_id = globalSQL.getInt("SELECT coordinate_id FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';");

            //Check is home with this name exists.
            if (!globalSQL.hasRow("SELECT uuid FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';")) {
                p.sendMessage(Utils.error("You do not have a home set with the name ")
                        .append(Component.text(name, NamedTextColor.DARK_RED)));
                return true;
            }

            //Delete home
            globalSQL.update("DELETE FROM home WHERE uuid='" + p.getUniqueId() + "' AND name='" + name + "';");
            p.sendMessage(Component.text(name, NamedTextColor.DARK_AQUA)
                    .append(Utils.success(" home removed.")));

            //Delete coordinate id.
            globalSQL.update("DELETE FROM coordinates WHERE id=" + coordinate_id + ";");

        }

        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command
            command, @NotNull String label, @NotNull String[] args) {

        List<String> homes = Network.getInstance().globalSQL.getStringList("SELECT name FROM home WHERE uuid='" + ((Player) sender).getUniqueId() + " AND name IS NOT NULL;");
        List<String> returns = new ArrayList<>();

        if (args.length == 0) {

            return homes;

        } else if (args.length == 1) {

            StringUtil.copyPartialMatches(args[0], homes, returns);
            return returns;

        } else {

            return null;

        }
    }
}
