package me.bteuk.network.commands;

import me.bteuk.network.utils.Roles;
import me.bteuk.network.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Help implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.chat("&cThis command can only be run by a player."));
            return true;

        }

        //Default message.
        if (args.length == 0) {

            help(p);

        }

        //Check for first arg to determine message.
        switch (args[0]) {

            //Building
            case "building" -> building(p);

            //Exploring
            case "explore" -> explore(p);

            //Utilities
            case "utils" -> utils(p);

            //Worldedit
            case "worldedit" -> worldedit(p);

            //Default for any other arguments.
            default -> help(p);

        }

        return true;
    }

    private void help(Player p) {
        //Navigator
        p.sendMessage(Utils.chat("&7/navigator &fOpens the navigator, access most server features from here."));

        //Building
        p.sendMessage(Utils.chat("&f/help building &fInformation on how to rank-up and build."));

        //Plots
        //Information about plots.

        //Regions
        //Information about regions.

        //Exporing
        p.sendMessage(Utils.chat("&f/help explore &fLists commands used for exploring the server."));

        //Utilities
        p.sendMessage(Utils.chat("&f/help utils &fOther commands that can be useful in general."));

        //Worldedit
        p.sendMessage(Utils.chat("&7/help worldedit &fLists available WorldEdit commands."));
    }

    private void building(Player p) {

        //Building is partially role-specific.
        p.sendMessage(Utils.chat("You currently have the builder role " + PlaceholderAPI.setPlaceholders(p, "%luckperms_prefix_highest_on_track_builder%")));

        switch (Roles.builderRole(p)) {

            case "architect" -> p.sendMessage(Utils.chat("The highest obtainable builder role, you can claim regions without needing staff approval, " +
                    "claim plots of all difficulties in the plot system and allows you to create new plots."));

            case "builder" -> p.sendMessage(Utils.chat("A &7Builder &fcan claim regions without needing staff approval as well as claims plots of all difficulties in the plot system. " +
                    "To be promoted to &7Architect &fyou need x building points in the 30 days."));

            case "jrbuilder" -> p.sendMessage(Utils.chat("&7Jr.Builder &fis the first role that has the ability to claim regions freely, however in busy areas reviewers will need to review the request. " +
                    "You also are able to claim plots of all difficulties in the plot system. Complete 1 hard plot to be promoted to &7Builder&f."));

            case "apprentice" -> p.sendMessage(Utils.chat("&7Apprentice &fis the first role achieved by building, while not giving access to region claims yet it is a step in the right direction. " +
                    "You are now able to claim plots of medium difficulty, complete 1 medium plot to be promoted to &7Jr.Builder&f."));

            case "applicant" -> p.sendMessage(Utils.chat("Achieved by completed the required steps in the tutorial, you not have access to the plot system. " +
                    "Complete 1 easy plot to be promoted to &7Apprentice&f."));

            case "guest" -> p.sendMessage(Utils.chat("The role you start with when you first join the server. To start your path towards becoming a builder just hop into the tutorial."));

        }

        //Plotsystem and region commands.
        //TODO: Implement /region and /plot fully.

        //Tpll and ll.
        p.sendMessage(Utils.chat("\n&7/tpll <lat> <lon> [altitude] &fTeleport to the coordinates provided, altitude is optional."));
        p.sendMessage(Utils.chat("&7/ll &fGet the real life coordinates of your current location, with a link to Google Maps."));

        p.sendMessage(Utils.chat("&7/skulls &fOpens the head menu. To search for a specific head use &7/skulls search"));
        p.sendMessage(Utils.chat("&7/bannermaker &fOpens the bannermaker menu, allows you to create and save banners easily."));

    }

    private void explore(Player p) {

        //Exploring using the gui or map.
        p.sendMessage(Utils.chat("Using the navigator &7/navigator &fyou can access many locations that are being or have been built."));
        p.sendMessage(Utils.chat("Alternatively you can use the map located in the lobby &7/lobby&f."));

        //Tpll can be used otherwise but for roles without region access they can't load new terrain.
        //For Jr.Builder also explain how to request new locations.
        switch (Roles.builderRole(p)) {

            case "apprentice", "applicant", "guest" -> p.sendMessage(Utils.chat("To access other areas you can try using &7/tpll <lat> <lon>&f, " +
                    "however you will only be able to teleport to locations that have already been generated on the server."));

            default -> {
                p.sendMessage(Utils.chat("You can request new locations to be added to the navigator, " +
                        "this can be done by standing at the location you want to add and then clicking on 'Add Location' in the exporation menu. " +
                        "Please only request locations with a decent bit of progress."));

                p.sendMessage(Utils.chat("To access other areas you can try using &7/tpll <lat> <lon>&f, " +
                        "however please don't generate new terrain for no good reason."));
            }
        }

        //Home command for saving personal locations.
        //TODO: Add /home

    }

    private void utils(Player p) {

    }

    private void worldedit(Player p) {

    }
}
