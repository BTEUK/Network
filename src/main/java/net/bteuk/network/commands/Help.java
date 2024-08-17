package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.FixedArgSelector;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.Role;
import net.bteuk.network.utils.Roles;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Help extends AbstractCommand {

    private static final Component ROLE_ERROR = ChatUtils.error("An error occurred while loading a role, please contact an administrator.");

    public Help(Network instance) {
        super(instance, "help");
        command.setTabCompleter(new FixedArgSelector(Arrays.asList("building", "explore", "plots", "regions", "utils", "worldedit"), 0));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(ChatUtils.error("This command can only be run by a player."));
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

        p.sendMessage(Utils.title("Help:"));

        //Navigator
        p.sendMessage(Component.text("/navigator", NamedTextColor.GRAY).append(Utils.line(" - Click to open the navigator, access most server features from here."))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/navigator")));

        //Exporing
        p.sendMessage(Component.text("/help explore", NamedTextColor.GRAY).append(Utils.line(" - Click to list commands used for exploring the server."))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/help explore")));

        //Building
        p.sendMessage(Component.text("/help building", NamedTextColor.GRAY).append(Utils.line(" - Click for information on how to rank-up and build."))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/help building")));

        //Plots
        //Information about plots.
        p.sendMessage(Component.text("/help plots", NamedTextColor.GRAY).append(Utils.line(" - Click for details on plots and how to use them."))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/help plots")));

        //Regions
        //Information about regions.
        p.sendMessage(Component.text("/help regions", NamedTextColor.GRAY).append(Utils.line(" - Click for information about regions and why we have them."))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/help regions")));

        //Utilities
        p.sendMessage(Component.text("/help utils", NamedTextColor.GRAY).append(Utils.line(" - Click for other commands that can be useful in general."))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/help utils")));

        //Worldedit
        p.sendMessage(Component.text("/help worldedit", NamedTextColor.GRAY).append(Utils.line(" - Click to list available WorldEdit commands."))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/help worldedit")));
    }

    private void building(Player p) {

        // Building is partially role-specific.
        // Get the current builder-role of the player.
        Role builderRole = Roles.builderRole(p);
        if (builderRole == null) {
            p.sendMessage(ChatUtils.error("You have an invalid role, please contact an administrator."));
            return;
        }

        p.sendMessage(Utils.title("Building on the server:"));

        Component roleMessage = Utils.line("You currently have the builder role ")
                .append(builderRole.getColouredRoleName());

        Component roleSpecificMessage = switch (builderRole.getId()) {
            case "reviewer" -> {
                Role architect = Roles.getRoleById("architect");
                if (architect == null) {
                    yield null;
                }
                yield ChatUtils.line("A ")
                        .append(builderRole.getColouredRoleName())
                        .append(ChatUtils.line(" has all the functions of an "))
                        .append(architect.getColouredRoleName())
                        .append(ChatUtils.line(" but can also review plots, region requests and navigation requests."));
            }

            case "architect" -> {
                Role builder = Roles.getRoleById("builder");
                if (builder == null) {
                    yield null;
                }
                yield ChatUtils.line("An ")
                        .append(builderRole.getColouredRoleName())
                        .append(ChatUtils.line(" has all the functions of a "))
                        .append(builder.getColouredRoleName())
                        .append(ChatUtils.line(" and can create zones in the plotsystem and create new plots."));
            }

            case "builder" -> ChatUtils.line("A ")
                    .append(builderRole.getColouredRoleName())
                    .append(ChatUtils.line(" can claim regions without needing staff approval as well as claim plots of all difficulties in the plot system. " /*+
                "To be promoted to &7Architect &fyou need x building points in the last 30 days."*/));

            case "jrbuilder" -> {
                Role builder = Roles.getRoleById("builder");
                if (builder == null) {
                    yield null;
                }
                yield builderRole.getColouredRoleName()
                        .append(ChatUtils.line(" is the first role that has the ability to claim regions freely, however in busy areas reviewers will need to review the request. " +
                                "Complete 1 hard plot to be promoted to "))
                        .append(builder.getColouredRoleName());
            }

            case "apprentice" -> {
                Role jrbuilder = Roles.getRoleById("jrbuilder");
                if (jrbuilder == null) {
                    yield null;
                }
                yield builderRole.getColouredRoleName()
                        .append(ChatUtils.line(" is the first role achieved by building, however you must rank up once more to have access to region claiming. " +
                                "Complete 1 normal plot to be promoted to "))
                        .append(jrbuilder.getColouredRoleName());
            }

            case "applicant" -> {
                Role apprentice = Roles.getRoleById("apprentice");
                if (apprentice == null) {
                    yield null;
                }
                yield builderRole.getColouredRoleName()
                        .append(ChatUtils.line(" is achieved by completing the required steps in the tutorial, you now have access to the plot system. " +
                                "Complete 1 easy plot to be promoted to "))
                        .append(apprentice.getColouredRoleName());
            }

            case "default" ->
                    ChatUtils.line("The role you start with when you first join the server. To start your path towards becoming a builder just hop into the tutorial.");

            default -> null;
        };

        if (roleSpecificMessage == null) {
            p.sendMessage(ROLE_ERROR);
            return;
        }

        p.sendMessage(roleMessage);
        p.sendMessage(roleSpecificMessage);

        //Tpll and ll.
        p.sendMessage(Component.text("/tpll <lat> <lon> [altitude]", NamedTextColor.GRAY)
                .append(Utils.line(" - Teleport to the coordinates provided, altitude is optional.")));
        p.sendMessage(Component.text("/ll", NamedTextColor.GRAY)
                .append(Utils.line(" - Get the real life coordinates of your current location, with a link to Google Maps.")));

        p.sendMessage(Component.text("/skulls", NamedTextColor.GRAY)
                .append(Utils.line(" - Opens the head menu. To search for a specific head use "))
                .append(Component.text("/skulls search")));
        p.sendMessage(Component.text("/bannermaker", NamedTextColor.GRAY)
                .append(Utils.line(" - Opens the bannermaker menu, allows you to create and save banners easily.")));

    }

    private void explore(Player p) {

        Role builderRole = Roles.builderRole(p);
        if (builderRole == null) {
            p.sendMessage(ChatUtils.error("You have an invalid role, please contact an administrator."));
            return;
        }

        p.sendMessage(Utils.title("Exploring the server:"));

        //Exploring using the gui or map.
        p.sendMessage(Utils.line("Using the navigator ")
                .append(Component.text("/navigator", NamedTextColor.GRAY))
                .append(Utils.line(" you can access many locations that are being or have been built on the server.")));
        p.sendMessage(Utils.line("Alternatively you can use the ")
                .append(Component.text("/map", NamedTextColor.GRAY))
                .append(Utils.line(" located in the "))
                .append(Component.text("/lobby", NamedTextColor.GRAY)));

        //Tpll can be used otherwise but for roles without region access they can't load new terrain.
        //For Jr.Builder also explain how to request new locations.

        switch (builderRole.getId()) {

            case "apprentice", "applicant", "default" ->
                    p.sendMessage(Utils.line("\nTo access other areas you can try using ")
                            .append(Component.text("/tpll <lat> <lon>", NamedTextColor.GRAY))
                            .append(Utils.line(", however you will only be able to teleport to locations that have already been generated on the server.")));

            default -> {
                p.sendMessage(Utils.line("\nYou can request new locations to be added to the navigator, " +
                        "this can be done by standing at the location you want to add and then clicking on 'Add Location' in the exporation menu. " +
                        "Please only request locations with a decent bit of progress."));

                p.sendMessage(Utils.line("\nTo access other areas you can try using ")
                        .append(Component.text("/tpll <lat> <lon>", NamedTextColor.GRAY))
                        .append(Utils.line(", however please don't generate new terrain for no good reason.")));
            }
        }

        //Home command for saving personal locations.
        switch (builderRole.getId()) {
            case "apprentice", "applicant", "default" -> p.sendMessage(Utils.line("\nYou can set a ")
                    .append(Component.text("/home", NamedTextColor.GRAY))
                    .append(Utils.line(" using "))
                    .append(Component.text("/sethome", NamedTextColor.GRAY)));

            default -> p.sendMessage(Utils.line("\nYou can set ")
                    .append(Component.text("/homes", NamedTextColor.GRAY))
                    .append(Utils.line(" using "))
                    .append(Component.text("/sethome <name>", NamedTextColor.GRAY)));
        }
    }

    private void plots(Player p) {

        // Get the roles needed for the text.
        Role applicant = Roles.getRoleById("applicant");
        Role apprentice = Roles.getRoleById("apprentice");
        Role jrbuilder = Roles.getRoleById("jrbuilder");
        Role builder = Roles.getRoleById("builder");

        if (applicant == null || apprentice == null || jrbuilder == null || builder == null) {
            p.sendMessage(ROLE_ERROR);
            return;
        }

        p.sendMessage(Utils.title("Plots:"));

        p.sendMessage(Utils.line("A plot will usually include a building or row of connected buildings. " +
                "The goal of a plot is to complete that building and then submit it."));

        p.sendMessage(Utils.line("\nYou can claim a plot in the building menu or using ")
                .append(Component.text("/claim", NamedTextColor.GRAY))
                .append(Utils.line(" while standing in a plot.")));

        p.sendMessage(Utils.line("\nPlots can be managed in the plot menu which is accessed from the build menu, from here you can also invite others to your plot."));

        p.sendMessage(Utils.line("\nBy completing plots of various difficulties you are able to rank up."));
        p.sendMessage(
                applicant.getColouredRoleName()
                        .append(Utils.line(" -> "))
                        .append(apprentice.getColouredRoleName())
                        .append(Utils.line(" by completing an easy plot.")));

        p.sendMessage(
                apprentice.getColouredRoleName()
                        .append(Utils.line(" -> "))
                        .append(jrbuilder.getColouredRoleName())
                        .append(Utils.line(" by completing a normal plot.")));

        p.sendMessage(
                jrbuilder.getColouredRoleName()
                        .append(Utils.line(" -> "))
                        .append(builder.getColouredRoleName())
                        .append(Utils.line(" by completing a hard plot.")));

        //p.sendMessage(Utils.chat("&7\nArchitects &fare able to create new plots, more info on this using &7/help architect&f."));

    }

    private void regions(Player p) {

        p.sendMessage(Utils.title("Regions:"));

        p.sendMessage(Utils.line("A region represents a 512 by 512 area in a grid of regions, they cover the whole world."));
        p.sendMessage(Utils.line("Regions provide an added layer of security and accountability in determining who has built where and when. " +
                "We keep track of every player who joins a region, this allows us to trace back any wrongdoers with relative ease."));

        Role jrbuilder = Roles.getRoleById("jrbuilder");
        p.sendMessage(Utils.line("\nTo join a region you must be a ")
                .append(jrbuilder.getColouredRoleName())
                .append(Utils.line(" or above, this is both to prevent griefers and to ensure building standards.")));

        p.sendMessage(Utils.line("\nYou can join a region by clicking on the dark oak door in the build menu. " +
                "If the region already has an owner then they must accept your request before you are able to build in the region."));
        p.sendMessage(Utils.line("Region owners can also invite you to their region using the gui, you will then be notified in chat."));

    }

    private void utils(Player p) {

        p.sendMessage(Utils.title("Utilities:"));

        p.sendMessage(Component.text("/tp <player>", NamedTextColor.GRAY)
                .append(Utils.line(" - Teleport to a specific player anywhere on the server.")));

        p.sendMessage(Component.text("/back", NamedTextColor.GRAY)
                .append(Utils.line(" - Return to the location prior to your last teleport.")));

        p.sendMessage(Component.text("\n/speed [0-10]", NamedTextColor.GRAY)
                .append(Utils.line(" - Set your flying/walking speed.")));

        p.sendMessage(Component.text("/nv", NamedTextColor.GRAY)
                .append(Utils.line(" - Toggle night vision, also removes glitched shadows.")));

        p.sendMessage(Component.text("\n/discord", NamedTextColor.GRAY)
                .append(Utils.line(" - Sends you a link to our Discord server.")));

    }

    private void worldedit(Player p) {

        p.sendMessage(Utils.title("WorldEdit:"));

        p.sendMessage(Component.text("//wand", NamedTextColor.GRAY)
                .append(Utils.line(" - Gives you the selection tool for WorldEdit.")));
        p.sendMessage(Utils.line("Left click to select your first point, right click to add a second (or more for certain selection types)."));

        p.sendMessage(Component.text("\n//set <block>", NamedTextColor.GRAY)
                .append(Utils.line(" - Sets the area you've selected to the specified block.")));

        p.sendMessage(Component.text("//replace <block> <block>", NamedTextColor.GRAY)
                .append(Utils.line(" - Replaces the specified block with another block in your selection.")));

        p.sendMessage(Component.text("//line <block>", NamedTextColor.GRAY)
                .append(Utils.line(" - Creates a line between your two selected points.")));

        p.sendMessage(Component.text("\n//undo", NamedTextColor.GRAY)
                .append(Utils.line(" and "))
                .append(Component.text("//redo", NamedTextColor.GRAY))
                .append(Utils.line(" allow you to undo or redo any WorldEdit command.")));

        Component worldEditMessage = Utils.line("For more information you can reference: ")
                .append(Component.text("https://worldedit.enginehub.org/en/latest/usage/", NamedTextColor.GRAY));
        worldEditMessage = worldEditMessage.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://worldedit.enginehub.org/en/latest/usage/"));
        p.sendMessage(worldEditMessage);

    }
}
