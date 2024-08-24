package net.bteuk.network.commands.navigation;

import net.bteuk.network.Network;
import net.bteuk.network.commands.AbstractCommand;
import net.bteuk.network.commands.tabcompleters.NavigationTabCompleter;
import net.bteuk.network.gui.navigation.AddLocation;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.enums.AddLocationType;
import net.bteuk.network.utils.enums.Category;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.bteuk.network.utils.Constants.LOGGER;

public class Navigation extends AbstractCommand {

    private static final Component ERROR_SUBCATEGORY_ADD = ChatUtils.error("/navigation subcategory add [category] <subcategory>");
    private static final Component ERROR_SUBCATEGORY_REMOVE = ChatUtils.error("/navigation subcategory remove <subcategory>");

    public Navigation(Network instance) {
        super(instance, "navigation");
        command.setTabCompleter(new NavigationTabCompleter());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //This command can only be used by a player.
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatUtils.error("This command can only be used by a player."));
            return true;
        }

        //If u is null, cancel.
        NetworkUser u = Network.getInstance().getUser(p);
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return true;
        }

        //Check if args is less than 1.
        if (args.length < 1) {
            //Send error message.
            error(u);
            return true;
        }

        //Add
        switch (args[0].toUpperCase()) {

            // Add location
            case "ADD" -> addLocation(u);

            // Update location
            case "UPDATE" -> updateLocation(u, args);

            // Remove location
            case "REMOVE" -> removeLocation(u, args);

            // Suggested location
            case "SUGGESTED" -> suggestedLocation(u, args);

            // Subcategory subcommands
            case "SUBCATEGORY" -> subcategoryCommand(u, args);

            default -> error(u);
        }

        return true;
    }

    private void addLocation(NetworkUser u) {
        if (u.hasPermission("uknet.navigation.request")) {
            if (u.mainGui != null) {
                u.mainGui.delete();
            }
            u.mainGui = new AddLocation(AddLocationType.ADD);
            u.mainGui.open(u);
        } else {
            u.sendMessage(ChatUtils.error("You do not have permission to use this command."));
        }
    }

    private void updateLocation(NetworkUser u, String[] args) {
        if (!u.hasPermission("uknet.navigation.update")) {
            u.sendMessage(ChatUtils.error("You do not have permission to use this command."));
            return;
        }

        if (args.length < 2) {
            u.sendMessage(ChatUtils.error("/navigation update <location>"));
            return;
        }

        // Combine all args excluding the first, with spaces, since the name can be multiple words.
        String location = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Check if the location exists.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {
            u.sendMessage(ChatUtils.error("The location ")
                    .append(Component.text(location, NamedTextColor.DARK_RED))
                    .append(ChatUtils.error(" does not exist.")));
            return;
        }

        // Check if there is a marker on the map.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT location FROM location_marker WHERE location='" + location + "';")) {
            u.sendMessage(ChatUtils.error("The location %s has a marker on the map, this must be removed first using %s",
                    location, String.format("/map remove %s", location)));
            return;
        }

        // Open update location menu.
        // They must be staff to access this.
        if (u.staffGui != null) {
            u.staffGui.delete();
        }

        // Get details from the location.
        Category category = Category.valueOf(Network.getInstance().getGlobalSQL().getString("SELECT category FROM location_data WHERE location='" + location + "';"));
        int subcategory_id = Network.getInstance().getGlobalSQL().getInt("SELECT subcategory FROM location_data WHERE location='" + location + "';");
        String subcategory = null;
        if (subcategory_id != 0) {
            subcategory = Network.getInstance().getGlobalSQL().getString("SELECT name FROM location_subcategory WHERE id=" + subcategory_id + ";");
        }
        int coordinate_id = Network.getInstance().getGlobalSQL().getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");
        u.staffGui = new AddLocation(AddLocationType.UPDATE, location, coordinate_id, category, subcategory);
        u.staffGui.open(u);
    }

    private void removeLocation(NetworkUser u, String[] args) {
        if (!u.hasPermission("uknet.navigation.remove")) {
            u.sendMessage(ChatUtils.error("You do not have permission to use this command."));
            return;
        }

        if (args.length < 2) {
            u.sendMessage(ChatUtils.error("/navigation remove <location>"));
            return;
        }

        //Combine all args excluding the first, with spaces, since the name can be multiple words.
        String location = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        //Check if the location exists.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {
            u.sendMessage(ChatUtils.error("The location ")
                    .append(Component.text(location, NamedTextColor.DARK_RED))
                    .append(ChatUtils.error(" does not exist.")));
            return;
        }

        // Check if there is a marker on the map.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT location FROM location_marker WHERE location='" + location + "';")) {
            u.sendMessage(ChatUtils.error("The location %s has a marker on the map, this must be removed first using %s",
                    location, String.format("/map remove %s", location)));
            return;
        }

        //Delete location.
        Network.getInstance().getGlobalSQL().update("DELETE FROM location_data WHERE location='" + location + "';");
        u.sendMessage(ChatUtils.success("Location ")
                .append(Component.text(location, NamedTextColor.DARK_AQUA))
                .append(ChatUtils.success(" removed.")));
    }

    private void suggestedLocation(NetworkUser u, String[] args) {
        if (u.hasPermission("uknet.navigation.suggested")) {
            if (args.length > 1) {
                //Combine all args excluding the first, with spaces, since the name can be multiple words.
                String location = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                //Check if the location exists.
                if (Network.getInstance().getGlobalSQL().hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {
                    //Change suggested status of location.
                    if (Network.getInstance().getGlobalSQL().hasRow("SELECT location FROM location_data WHERE location='" + location + "' AND suggested=1;")) {
                        //Location is already suggested, remove that.
                        Network.getInstance().getGlobalSQL().update("UPDATE location_data SET suggested=0 WHERE location='" + location + "';");
                        u.sendMessage(ChatUtils.success("The location ")
                                .append(Component.text(location, NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(" will no longer be suggested.")));
                    } else {
                        //Set location as suggested.
                        Network.getInstance().getGlobalSQL().update("UPDATE location_data SET suggested=1 WHERE location='" + location + "';");
                        u.sendMessage(ChatUtils.success("The location ")
                                .append(Component.text(location, NamedTextColor.DARK_AQUA))
                                .append(ChatUtils.success(" will now be suggested.")));
                    }
                } else {
                    u.sendMessage(ChatUtils.error("The location ")
                            .append(Component.text(location, NamedTextColor.DARK_RED))
                            .append(ChatUtils.error(" does not exist.")));
                }
            } else {
                u.sendMessage(ChatUtils.error("/navigation suggested <location>"));
            }
        } else {
            u.sendMessage(ChatUtils.error("You do not have permission to use this command."));
        }
    }

    private void subcategoryCommand(NetworkUser u, String[] args) {
        if (!u.hasPermission("uknet.navigation.subcategory")) {
            u.sendMessage(ChatUtils.error("You do not have permission to use this command."));
            return;
        }

        if (args.length < 3) {
            errorSubcategory(u);
            return;
        }

        switch (args[1].toUpperCase()) {

            // Add
            case "ADD" -> addSubcategory(u, args);

            // Remove
            case "REMOVE" -> removeSubcategory(u, args);

            default -> errorSubcategory(u);
        }
    }

    private void addSubcategory(NetworkUser u, String[] args) {
        if (args.length > 3) {
            // Check if arg[2] is a valid category.
            if (Arrays.stream(Category.values()).filter(Category::isSelectable).anyMatch(category -> category.toString().equalsIgnoreCase(args[2]))) {
                // Check that the subcategory does not yet exist.
                String name = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                if (Network.getInstance().getGlobalSQL().hasRow("SELECT name FROM location_subcategory WHERE name='" + name + "';")) {
                    u.sendMessage(ChatUtils.error("Subcategory ").append(Component.text(name, NamedTextColor.DARK_RED)).append(ChatUtils.error(" already exists.")));
                } else {
                    Network.getInstance().getGlobalSQL().update("INSERT INTO location_subcategory(name,category) VALUES('" + name + "','" + args[2].toUpperCase() + "');");
                    u.sendMessage(ChatUtils.success("Subcategory ").append(Component.text(name, NamedTextColor.DARK_AQUA))
                            .append(ChatUtils.success(" added to category ")).append(Component.text(args[2].toUpperCase(), NamedTextColor.DARK_AQUA)));
                }
            } else {
                u.sendMessage(ChatUtils.error("Category ").append(Component.text(args[2], NamedTextColor.DARK_RED).append(ChatUtils.error(" is not valid."))));
            }
        } else {
            u.sendMessage(ERROR_SUBCATEGORY_ADD);
        }
    }

    private void removeSubcategory(NetworkUser u, String[] args) {
        if (args.length < 3) {
            u.sendMessage(ERROR_SUBCATEGORY_REMOVE);
        }

        // Check if a valid subcategory is listed.
        String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        int subcategory_id = Network.getInstance().getGlobalSQL().getInt("SELECT name FROM location_subcategory WHERE name='" + name + "';");

        if (subcategory_id == 0) {
            u.sendMessage(ChatUtils.error("Subcategory ").append(Component.text(name, NamedTextColor.DARK_RED)).append(ChatUtils.error(" does not exist.")));
            return;
        }

        // Check if there is a marker on the map.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT subcategory FROM location_marker WHERE subcategory=" + subcategory_id + ";")) {
            u.sendMessage(ChatUtils.error("The subcategory %s has a marker on the map, this must be removed first using %s",
                    name, String.format("/map remove %s", name)));
            return;
        }

        // Set all locations (and requests) with this subcategory to subcategory = null.
        Network.getInstance().getGlobalSQL().update("UPDATE location_data SET subcategory=NULL WHERE subcategory=" + subcategory_id + ";");
        Network.getInstance().getGlobalSQL().update("UPDATE location_requests SET subcategory=NULL WHERE subcategory=" + subcategory_id + ";");
        // Remove the subcategory.
        Network.getInstance().getGlobalSQL().update("DELETE FROM location_subcategory WHERE id=" + subcategory_id + ";");
        u.sendMessage(ChatUtils.error("Subcategory ").append(Component.text(name, NamedTextColor.DARK_RED)).append(ChatUtils.error(" removed.")));

    }

    private void error(NetworkUser u) {

        //If the player has permission for any of the commands send them the error.
        //Else tell them they don't have permission.
        if (u.hasAnyPermission(new String[]{"uknet.navigation.request", "uknet.navigation.update", "uknet.navigation.remove", "uknet.navigation.suggested", "uknet.navigation.subcategory"})) {
            if (u.hasPermission("uknet.navigation.request")) {
                u.sendMessage(ChatUtils.error("/navigation add"));
            }

            if (u.hasPermission("uknet.navigation.update")) {
                u.sendMessage(ChatUtils.error("/navigation update <location>"));
            }


            if (u.hasPermission("uknet.navigation.remove")) {
                u.sendMessage(ChatUtils.error("/navigation remove <location>"));
            }

            if (u.hasPermission("uknet.navigation.suggested")) {
                u.sendMessage(ChatUtils.error("/navigation suggested <location>"));
            }

            if (u.hasPermission("uknet.navigation.subcategory")) {
                errorSubcategory(u);
            }
        } else {
            u.sendMessage(ChatUtils.error("You do not have permission to use this command."));
        }
    }

    private void errorSubcategory(NetworkUser u) {
        u.sendMessage(ERROR_SUBCATEGORY_ADD);
        u.sendMessage(ERROR_SUBCATEGORY_REMOVE);
    }
}
