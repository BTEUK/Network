package me.bteuk.network.gui;

import me.bteuk.network.Network;
import me.bteuk.network.commands.navigation.Back;
import me.bteuk.network.commands.Nightvision;
import me.bteuk.network.events.EventManager;
import me.bteuk.network.gui.navigation.ExploreGui;
import me.bteuk.network.utils.LightsOut;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ServerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import static me.bteuk.network.utils.Constants.SERVER_TYPE;

public class NavigatorGui extends Gui {

    public NavigatorGui() {

        super(27, Component.text("Navigator", NamedTextColor.AQUA, TextDecoration.BOLD));

        setItem(3, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.title("Build"),
                        Utils.line("Click to open the build menu.")),
                u -> {

                    //Switch to the build menu.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);

                });

        setItem(5, Utils.createItem(Material.SPRUCE_BOAT, 1,
                        Utils.title("Explore"),
                        Utils.line("Click to open the explore menu.")),
                u -> {

                    //Click Action
                    u.mainGui = new ExploreGui(u);
                    u.mainGui.open(u);

                });

        setItem(26, Utils.createItem(Material.NETHER_STAR, 1,
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

        setItem(25, Utils.createPotion(Material.SPLASH_POTION, PotionEffectType.NIGHT_VISION, 1,
                        Utils.title("Toggle Nightvision"),
                        Utils.line("Click to toggle nightvision."),
                        Utils.line("You can also use the command ")
                                .append(Component.text("/nightvision", NamedTextColor.GRAY))
                                .append(Utils.line(" or "))
                                .append(Component.text("/nv", NamedTextColor.GRAY))),
                u -> Nightvision.toggleNightvision(u.player));

        setItem(19, Utils.createItem(Material.REDSTONE_LAMP, 1,
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
        setItem(21, Utils.createItem(Material.ENCHANTED_BOOK, 1,
                        Utils.title("Rules"),
                        Utils.line("Click to view the rules.")),
                u -> {

                    u.player.closeInventory();
                    u.player.openBook(Network.getInstance().getLobby().getRules());

                });

        //Spawn
        setItem(23, Utils.createItem(Material.RED_BED, 1,
                        Utils.title("Spawn"),
                        Utils.line("Teleport to spawn.")),
                u ->

                {

                    u.player.closeInventory();

                    //If server is Lobby, teleport to spawn.
                    if (SERVER_TYPE == ServerType.LOBBY) {

                        Back.setPreviousCoordinate(u.player.getUniqueId().toString(), u.player.getLocation());
                        u.player.teleport(Network.getInstance().getLobby().spawn);
                        u.player.sendMessage(Utils.success("Teleported to spawn."));

                    } else {

                        //Set teleport event to go to spawn.
                        EventManager.createTeleportEvent(true, u.player.getUniqueId().toString(), "network", "teleport spawn", u.player.getLocation());
                        SwitchServer.switchServer(u.player, Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='LOBBY';"));

                    }

                });


    }

    //This methods is not needed in this class, so it is empty.
    @Override
    public void refresh() {
    }
}
