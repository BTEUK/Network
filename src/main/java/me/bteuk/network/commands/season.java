package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class season extends AbstractCommand {

    private static final Component INVALID_COMMAND_ARGUMENTS = Utils.error("/season create|start|end [season_name]");

    public season(Network instance) {
        super(instance, "season");
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //Get the player, this is only to check the permission, it is not necessary to use the command.
        Player p = getPlayer(sender);

        if (p != null) {
            if (!p.hasPermission("uknet.season")) {
                p.sendMessage(NO_PERMISSION);
                return true;
            }
        }

        //Check args.
        if (args.length > 2) {

            //Check first arg.
            if (args[0].equalsIgnoreCase("create")) {

                //Create season with all remaining args as name.
                sender.sendMessage(createSeason(args));

            } else if (args[0].equalsIgnoreCase("start")) {

                //Start season with all remaining args as name.
                sender.sendMessage(startSeason(args));

            } else if (args[0].equalsIgnoreCase("end")) {

                //End season with all remaining args as name.
                sender.sendMessage(endSeason(args));

            }
        }

        //If the code reaches here then the command format was invalid.
        sender.sendMessage(INVALID_COMMAND_ARGUMENTS);
        return true;

    }

    private Component createSeason(String[] args) {

        //Get the name from the remaining args.
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        //Create the season if it does not already exist.
        if (Network.getInstance().globalSQL.hasRow("SELECT id FROM seasons WHERE id='" + name + "';")) {
            return Utils.error("A season with the name ")
                    .append(Component.text(name, NamedTextColor.DARK_RED)
                            .append(Utils.error(" already exists.")));
        }

        if (Network.getInstance().globalSQL.update("INSERT INTO seasons(id) VALUES('" + name + "');")) {
            return Utils.success("Season create with name ")
                    .append(Component.text(name, NamedTextColor.DARK_AQUA));
        } else {
            return Utils.success("An error occurred while creating the season, please contact a server admin.");
        }
    }

    private Component startSeason(String[] args) {

        //Get the name from the remaining args.
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        //Check if the season exists, and is not already active.
        if (!Network.getInstance().globalSQL.hasRow("SELECT id FROM seasons WHERE id='" + name + "';")) {
            return Utils.error("A season with the name ")
                    .append(Component.text(name, NamedTextColor.DARK_RED)
                            .append(Utils.error(" doesn't exists.")));
        } else if (Network.getInstance().globalSQL.hasRow("SELECT id FROM seasons WHERE id='" + name + "' AND active=1;")) {
            return Utils.error("The season ")
                    .append(Component.text(name, NamedTextColor.DARK_RED)
                            .append(Utils.error(" has already started.")));
        }

        if (Network.getInstance().globalSQL.update("UPDATE seasons SET active=1 WHERE id='" + name + "';")) {
            return Utils.success("Started season ")
                    .append(Component.text(name, NamedTextColor.DARK_AQUA));
        } else {
            return Utils.success("An error occurred while starting the season, please contact a server admin.");
        }
    }

    private Component endSeason(String[] args) {

        //Get the name from the remaining args.
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        //Check if the season exists, and is active.
        if (!Network.getInstance().globalSQL.hasRow("SELECT id FROM seasons WHERE id='" + name + "';")) {
            return Utils.error("A season with the name ")
                    .append(Component.text(name, NamedTextColor.DARK_RED)
                            .append(Utils.error(" doesn't exists.")));
        } else if (!Network.getInstance().globalSQL.hasRow("SELECT id FROM seasons WHERE id='" + name + "' AND active=1;")) {
            return Utils.error("The season ")
                    .append(Component.text(name, NamedTextColor.DARK_RED)
                            .append(Utils.error(" is not active.")));
        }

        if (Network.getInstance().globalSQL.update("UPDATE seasons SET active=0 WHERE id='" + name + "';")) {
            return Utils.success("Ended season ")
                    .append(Component.text(name, NamedTextColor.DARK_AQUA));
        } else {
            return Utils.success("An error occurred while starting the season, please contact a server admin.");
        }
    }
}
