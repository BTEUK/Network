package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Nightvision;
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
                        Utils.chat("&b&lBuild"),
                        Utils.chat("&fClick to open the build menu.")),
                u -> {

                    //Switch to the build menu.
                    u.buildGui = new BuildGui(u);
                    u.buildGui.open(u);

                });

        setItem(14, Utils.createItem(Material.SPRUCE_BOAT, 1,
                        Utils.chat("&b&lExplore"),
                        Utils.chat("&fClick to open the explore menu.")),
                u -> {

                    //Click Action

                });

        setItem(8, Utils.createItem(Material.NETHER_STAR, 1,
                        Utils.chat("&b&lToggle Navigator"),
                        Utils.chat("&fClick to toggle the navigator in your inventory."),
                        Utils.chat("&fYou can always open this menu with /navigator")),
                u -> {

                    if (u.navigator) {

                        //Set navigator to false and remove the navigator from the inventory.
                        u.navigator = false;
                        u.player.getInventory().setItem(8, null);

                        //Disable navigator in database.
                        Network.getInstance().globalSQL.update("UPDATE player_data SET navigator=0 WHERE uuid='" + u.player.getUniqueId() + "';");

                        u.player.sendMessage(Utils.chat("&aDisabled navigator in inventory."));

                    } else {

                        //Set navigator to true.
                        u.navigator = true;

                        //Enable navigator in database.
                        Network.getInstance().globalSQL.update("UPDATE player_data SET navigator=1 WHERE uuid='" + u.player.getUniqueId() + "';");

                        u.player.sendMessage(Utils.chat("&aEnabled navigator in inventory."));
                    }

                });

        setItem(7, Utils.createPotion(Material.SPLASH_POTION, PotionEffectType.NIGHT_VISION, 1,
                        Utils.chat("&b&lToggle Nightvision"),
                        Utils.chat("&fClick to toggle nightvision."),
                        Utils.chat("&fYou can also use the command &7/nightvision &for &7/nv&f.")),
                u -> {

                    Nightvision.toggleNightvision(u.player);

                });
    }
}
