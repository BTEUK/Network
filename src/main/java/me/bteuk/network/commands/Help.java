package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Roles;
import me.bteuk.network.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
            return true;

        }

        //Check for first arg to determine message.
        switch (args[0]) {

            //Building
            case "building" -> building(p);

            //Exploring
            case "explore" -> explore(p);

            //Plots
            case "plots" -> plots(p);

            //Regions
            case "regions" -> regions(p);

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
        p.sendMessage(Utils.chat("&7/navigator &f- Opens the navigator, access most server features from here."));

        //Exporing
        p.sendMessage(Utils.chat("&7/help explore &f- Lists commands used for exploring the server."));

        //Building
        p.sendMessage(Utils.chat("&7/help building &f- Information on how to rank-up and build."));

        //Plots
        //Information about plots.
        p.sendMessage(Utils.chat("&7/help plots &f- Details on plots and how to use them."));

        //Regions
        //Information about regions.
        p.sendMessage(Utils.chat("&7/help regions &f- What are regions and why we have them."));

        //Utilities
        p.sendMessage(Utils.chat("&7/help utils &f- Other commands that can be useful in general."));

        //Worldedit
        p.sendMessage(Utils.chat("&7/help worldedit &f- Lists available WorldEdit commands."));
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

        //Tpll and ll.
        p.sendMessage(Utils.chat("\n&7/tpll <lat> <lon> [altitude] &f- Teleport to the coordinates provided, altitude is optional."));
        p.sendMessage(Utils.chat("&7/ll &f- Get the real life coordinates of your current location, with a link to Google Maps."));

        p.sendMessage(Utils.chat("&7/skulls &f- Opens the head menu. To search for a specific head use &7/skulls search"));
        p.sendMessage(Utils.chat("&7/bannermaker &f- Opens the bannermaker menu, allows you to create and save banners easily."));

    }

    private void explore(Player p) {

        p.sendMessage(Utils.chat("&b&lExploring the server:"));

        //Exploring using the gui or map.
        p.sendMessage(Utils.chat("\nUsing the navigator &7/navigator &fyou can access many locations that are being or have been built on the server."));
        p.sendMessage(Utils.chat("Alternatively you can use the map located in the lobby &7/lobby&f."));

        //Tpll can be used otherwise but for roles without region access they can't load new terrain.
        //For Jr.Builder also explain how to request new locations.
        switch (Roles.builderRole(p)) {

            case "apprentice", "applicant", "guest" -> p.sendMessage(Utils.chat("\nTo access other areas you can try using &7/tpll <lat> <lon>&f, " +
                    "however you will only be able to teleport to locations that have already been generated on the server."));

            default -> {
                p.sendMessage(Utils.chat("\nYou can request new locations to be added to the navigator, " +
                        "this can be done by standing at the location you want to add and then clicking on 'Add Location' in the exporation menu. " +
                        "Please only request locations with a decent bit of progress."));

                p.sendMessage(Utils.chat("\nTo access other areas you can try using &7/tpll <lat> <lon>&f, " +
                        "however please don't generate new terrain for no good reason."));
            }
        }

        //Home command for saving personal locations.
        //TODO: Add /home

    }

    private void plots(Player p) {

        p.sendMessage(Utils.chat("&b&lPlots:"));

        p.sendMessage(Utils.chat("\nA plot will usually include a building or row of connected buildings. " +
                "The goal of a plot is to complete that building and then submit it."));

        p.sendMessage(Utils.chat("\nYou can claim a plot in the building menu or using &7/claim &fwhile standing in a plot."));

        p.sendMessage(Utils.chat("\nPlots can be managed in the plot menu which is accessed from the build menu, from here you can also invite others to your plot."));

        p.sendMessage(Utils.chat("\nBy completing plots of various difficulties you are able to rank up."));
        p.sendMessage(Utils.chat("&7Applicant &f-> &7Apprentice &fby completing an easy plot."));
        p.sendMessage(Utils.chat("&7Apprentice &f-> &7Jr.Builder &fby completing a medium plot."));
        p.sendMessage(Utils.chat("&7Jr.Builder &f-> &7Builder &fby completing a hard plot."));

        p.sendMessage(Utils.chat("&7\nArchitects &fare able to create new plots, more info on this using &7/help architect&f."));

    }

    private void regions(Player p) {

        p.sendMessage(Utils.chat("&b&lRegions:"));

        p.sendMessage(Utils.chat("\nA region represents a &7512 by 512 area &fin a grid of regions, they cover the whole world."));
        p.sendMessage(Utils.chat("Regions provide an added layer of security and accountability in determining who has built where and when. " +
                "We keep track of every player who joins a region, this allows us to trace back any wrongdoers with relative ease."));

        p.sendMessage(Utils.chat("\nTo join a region you must be a &7Jr.Builder &for above, this is both to prevent griefers and to ensure building standards."));

        p.sendMessage(Utils.chat("\nYou can join a region by clicking on the dark oak door in the build menu. " +
                "If the region already has an owner then they must accept your request before you are able to build in the region."));
        p.sendMessage(Utils.chat("Region owners can also invite you to their region using the gui, you will then be notified in chat."));

    }

    private void utils(Player p) {

        p.sendMessage(Utils.chat("&b&lUtilities:"));

        p.sendMessage(Utils.chat("\n&7/tp <player> &f- Teleport to a specific player anywhere on the server."));
        p.sendMessage(Utils.chat("&7/back &fReturn to the location prior to your last teleport."));

        p.sendMessage(Utils.chat("\n&7/speed [0-10] &f- Set your flying/walking speed."));
        p.sendMessage(Utils.chat("&7/nv &f- Toggle night vision, also removes glitched shadows."));

        p.sendMessage(Utils.chat("\n&7/discord &f- Sends you a link to our Discord server."));
        p.sendMessage(Utils.chat("&7/modpack &f- Sends you links for the modpack download, makes the 1.12.2 world more stable."));

    }

    private void worldedit(Player p) {

        p.sendMessage(Utils.chat("&b&lWorldEdit:"));

        p.sendMessage(Utils.chat("\n&7//wand &f- Gives you the selection tool for WorldEdit."));
        p.sendMessage(Utils.chat("&7Left click to select your first point, right click to add a second (or more for certain selection types)."));

        p.sendMessage(Utils.chat("\n&7//set <block> &f- Sets the area you've selected to the specified block."));
        p.sendMessage(Utils.chat("&7//replace <block> <block> &f- Replaces the specified block with another block in your selection."));
        p.sendMessage(Utils.chat("&7//line <block> &f- Creates a line between your two selected points."));

        p.sendMessage(Utils.chat("\n&7//undo &f and &7//redo &f allow you to undo or redo any WorldEdit command."));

        TextComponent discord = new TextComponent(Utils.chat("\n&fFor more information you can reference: &7https://worldedit.enginehub.org/en/latest/usage/"));
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://worldedit.enginehub.org/en/latest/usage/"));
        p.spigot().sendMessage(discord);

    }
}
