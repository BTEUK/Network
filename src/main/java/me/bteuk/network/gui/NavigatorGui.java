package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Nightvision;
import me.bteuk.network.gui.navigation.ExploreGui;
import me.bteuk.network.utils.LightsOut;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public class NavigatorGui extends Gui {

    public NavigatorGui() {

        super(27, Component.text("Navigator", NamedTextColor.AQUA, TextDecoration.BOLD));

        setItem(12, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.title("Build"),
                        Utils.line("Click to open the build menu.")),
                u -> {

                    //Switch to the build menu.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);

                });

        setItem(14, Utils.createItem(Material.SPRUCE_BOAT, 1,
                        Utils.title("Explore"),
                        Utils.line("Click to open the explore menu.")),
                u -> {

                    //Click Action
                    u.mainGui = new ExploreGui(u);
                    u.mainGui.open(u);

                });

        setItem(8, Utils.createItem(Material.NETHER_STAR, 1,
                        Utils.title("Toggle Navigator"),
                        Utils.line("Click to toggle the navigator in your inventory."),
                        Utils.line("You can always open this menu with ")
                                .append(Component.text("/navigator", NamedTextColor.GRAY))),
                u -> {

                    if (u.navigator) {

                        //Set navigator to false and remove the navigator from the inventory.
                        u.navigator = false;
                        u.player.getInventory().setItem(8, null);

                        //Disable navigator in database.
                        Network.getInstance().globalSQL.update("UPDATE player_data SET navigator=0 WHERE uuid='" + u.player.getUniqueId() + "';");

                        u.player.sendMessage(Utils.success("Disabled navigator in inventory."));

                    } else {

                        //Set navigator to true.
                        u.navigator = true;

                        //Enable navigator in database.
                        Network.getInstance().globalSQL.update("UPDATE player_data SET navigator=1 WHERE uuid='" + u.player.getUniqueId() + "';");

                        u.player.sendMessage(Utils.success("Enabled navigator in inventory."));
                    }

                });

        setItem(7, Utils.createPotion(Material.SPLASH_POTION, PotionEffectType.NIGHT_VISION, 1,
                        Utils.title("Toggle Nightvision"),
                        Utils.line("Click to toggle nightvision."),
                        Utils.line("You can also use the command ")
                                .append(Component.text("/nightvision", NamedTextColor.GRAY))
                                .append(Utils.line(" or "))
                                .append(Component.text("/nv", NamedTextColor.GRAY))),
                u -> Nightvision.toggleNightvision(u.player));

        setItem(0, Utils.createItem(Material.REDSTONE_LAMP, 1,
                        Utils.title("Lights Out"),
                        Utils.line("Play a game of Lights Out.")),
                u -> {

                    if (u.lightsOut == null) {

                        u.lightsOut = new LightsOut(u);
                        u.lightsOut.open(u);

                    } else {

                        u.lightsOut.open(u);

                    }
                });

        //Set rules.
        setItem(26, Utils.createItem(Material.ENCHANTED_BOOK, 1,
                        Utils.title("Rules"),
                        Utils.line("Click to view the rules.")),
                u -> {

                    u.player.closeInventory();
                    u.player.openBook(Network.getInstance().getLobby().getRules());

                });


    }

    //This methods is not needed in this class, so it is empty.
    @Override
    public void refresh() {
    }
}
