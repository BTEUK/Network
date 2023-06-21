package me.bteuk.network.commands.navigation;

import me.bteuk.network.Network;
import me.bteuk.network.gui.navigation.AddLocation;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.AddLocationType;
import me.bteuk.network.utils.enums.Categories;
import me.bteuk.network.utils.enums.Regions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static me.bteuk.network.utils.Constants.LOGGER;

public class Navigation implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //This command can only be used by a player.
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;
        }

        //Check if args is less than 1.
        if (args.length < 1) {
            //Send error message.
            error(p);
            return true;
        }

        //If u is null, cancel.
        NetworkUser u = Network.getInstance().getUser(p);
        if (u == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage("User can not be found, please relog!");
            return true;
        }

        //Add
        if (args[0].equalsIgnoreCase("add")) {
            if (p.hasPermission("uknet.navigation.request")) {
                if (u.mainGui != null) {
                    u.mainGui.delete();
                }
                u.mainGui = new AddLocation(AddLocationType.ADD);
                u.mainGui.open(u);
            } else {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
            }
        }

        //Update
        if (args[0].equalsIgnoreCase("update")) {
            if (p.hasPermission("uknet.navigation.update")) {
                if (args.length > 1) {
                    //Combine all args excluding the first, with spaces, since the name can be multiple words.
                    String location = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                    //Check if the location exists.
                    if (Network.getInstance().globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {
                        //Open update location menu.
                        //They must be staff to access this.
                        if (u.staffGui != null) {
                            u.staffGui.delete();
                        }
                        //Get details from the location.
                        Categories category = Categories.valueOf(Network.getInstance().globalSQL.getString("SELECT category FROM location_data WHERE location='" + location + "';"));
                        Regions subcategory = null;
                        if (category == Categories.ENGLAND) {
                            //Get subcategory.
                            subcategory = Regions.valueOf(Network.getInstance().globalSQL.getString("SELECT subcategory FROM location_data WHERE location='" + location + "';"));
                        }
                        int coordinate_id = Network.getInstance().globalSQL.getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");
                        u.staffGui = new AddLocation(AddLocationType.UPDATE, location, coordinate_id, category, subcategory);
                        u.staffGui.open(u);
                    } else {
                        p.sendMessage(Utils.error("The location ")
                                .append(Component.text(location, NamedTextColor.DARK_RED))
                                .append(Utils.error(" does not exist.")));
                    }
                } else {
                    p.sendMessage(Utils.error("/navigation update <location>"));
                }
            } else {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
            }
        }

        //Remove <location>
        if (args[0].equalsIgnoreCase("remove")) {
            if (p.hasPermission("uknet.navigation.remove")) {
                if (args.length > 1) {
                    //Combine all args excluding the first, with spaces, since the name can be multiple words.
                    String location = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                    //Check if the location exists.
                    if (Network.getInstance().globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {
                        //Delete location.
                        Network.getInstance().globalSQL.update("DELETE FROM location_data WHERE location='" + location + "';");
                        p.sendMessage(Utils.success("Location ")
                                .append(Component.text(location, NamedTextColor.DARK_AQUA))
                                .append(Utils.error(" removed.")));
                    } else {
                        p.sendMessage(Utils.error("The location ")
                                .append(Component.text(location, NamedTextColor.DARK_RED))
                                .append(Utils.error(" does not exist.")));
                    }
                } else {
                    p.sendMessage(Utils.error("/navigation remove <location>"));
                }
            } else {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
            }
        }

        //Suggested <location>
        if (args[0].equalsIgnoreCase("suggested")) {
            if (p.hasPermission("uknet.navigation.suggested")) {
                if (args.length > 1) {
                    //Combine all args excluding the first, with spaces, since the name can be multiple words.
                    String location = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                    //Check if the location exists.
                    if (Network.getInstance().globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + location + "';")) {
                        //Change suggested status of location.
                        if (Network.getInstance().globalSQL.hasRow("SELECT location FROM location_data WHERE location='" + location + "' AND suggested=1;")) {
                            //Location is already suggested, remove that.
                            Network.getInstance().globalSQL.update("UPDATE location_data SET suggested=0 WHERE location='" + location + "';");
                            p.sendMessage(Utils.success("The location ")
                                    .append(Component.text(location, NamedTextColor.DARK_AQUA))
                                    .append(Utils.error(" will no longer be suggested.")));
                        } else {
                            //Set location as suggested.
                            Network.getInstance().globalSQL.update("UPDATE location_data SET suggested=1 WHERE location='" + location + "';");
                            p.sendMessage(Utils.success("The location ")
                                    .append(Component.text(location, NamedTextColor.DARK_AQUA))
                                    .append(Utils.error(" will now be suggested.")));
                        }
                    } else {
                        p.sendMessage(Utils.error("The location ")
                                .append(Component.text(location, NamedTextColor.DARK_RED))
                                .append(Utils.error(" does not exist.")));
                    }
                } else {
                    p.sendMessage(Utils.error("/navigation suggested <location>"));
                }
            } else {
                p.sendMessage(Utils.error("You do not have permission to use this command."));
            }
        }

        return true;
    }

    public void error(Player p) {

        //If the player has permission for any of the commands send them the error.
        //Else tell them they don't have permission.
        if (p.hasPermission("uknet.navigation.request") || p.hasPermission("uknet.navigation.suggested") || p.hasPermission("uknet.navigation.remove") || p.hasPermission("uknet.navigation.update")) {
            if (p.hasPermission("uknet.navigation.request")) {
                p.sendMessage(Utils.error("/navigation add"));
            }

            if (p.hasPermission("uknet.navigation.update")) {
                p.sendMessage(Utils.error("/navigation update <location>"));
            }


            if (p.hasPermission("uknet.navigation.remove")) {
                p.sendMessage(Utils.error("/navigation remove <location>"));
            }

            if (p.hasPermission("uknet.navigation.suggested")) {
                p.sendMessage(Utils.error("/navigation suggested <location>"));
            }
        } else {
            p.sendMessage(Utils.error("You do not have permission to use this command."));
        }

    }
}
