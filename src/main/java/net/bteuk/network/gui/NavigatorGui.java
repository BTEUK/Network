package net.bteuk.network.gui;

import net.bteuk.network.Network;
import net.bteuk.network.commands.navigation.Back;
import net.bteuk.network.commands.Nightvision;
import net.bteuk.network.eventing.events.EventManager;
import net.bteuk.network.gui.navigation.ExploreGui;
import net.bteuk.network.utils.LightsOut;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.ServerType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import static net.bteuk.network.utils.Constants.SERVER_TYPE;
import static net.bteuk.network.utils.Constants.TUTORIALS;

public class NavigatorGui extends Gui {

    public NavigatorGui() {

        super(27, Component.text("Navigator", NamedTextColor.AQUA, TextDecoration.BOLD));

        setItem(2, Utils.createItem(Material.DIAMOND_PICKAXE, 1,
                        Utils.title("Build"),
                        Utils.line("Click to open the build menu.")),
                u -> {

                    //Switch to the build menu.
                    u.mainGui = new BuildGui(u);
                    u.mainGui.open(u);

                });

        setItem(4, Utils.createItem(Material.SPRUCE_BOAT, 1,
                        Utils.title("Explore"),
                        Utils.line("Click to open the explore menu.")),
                u -> {

                    //Click Action
                    u.mainGui = new ExploreGui(u);
                    u.mainGui.open(u);

                });

        setItem(6, Utils.createItem(Material.KNOWLEDGE_BOOK, 1,
                        Utils.title("Tutorials"),
                        Utils.line("Click to open the tutorials menu.")),
                u -> {

                    //Switch to tutorials menu if it's online and enabled.
                    //If the current server is already tutorials, don't open the gui.
                    if (SERVER_TYPE == ServerType.TUTORIAL) {

                        u.player.closeInventory();
                        u.player.sendMessage(Utils.error("You are already in the tutorials server, please use the menu in slot 8."));

                    } else {
                        if (TUTORIALS) {
                            if (Network.getInstance().getGlobalSQL().hasRow("SELECT name FROM server_data WHERE type='TUTORIAL' AND online=1;")) {

                                u.mainGui = new TutorialsGui(u);
                                u.mainGui.open(u);

                            } else {
                                u.player.closeInventory();
                                u.player.sendMessage(Utils.error("The tutorials server is offline!"));
                            }
                        } else {
                            u.player.closeInventory();
                            u.player.sendMessage(Utils.error("Tutorials are currently not enabled!"));
                        }
                    }

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
                        Network.getInstance().getGlobalSQL().update("UPDATE player_data SET navigator=0 WHERE uuid='" + u.player.getUniqueId() + "';");

                        u.player.sendMessage(Utils.success("Disabled navigator in inventory."));

                    } else {

                        //Set navigator to true.
                        u.navigator = true;

                        //Enable navigator in database.
                        Network.getInstance().getGlobalSQL().update("UPDATE player_data SET navigator=1 WHERE uuid='" + u.player.getUniqueId() + "';");

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
                        SwitchServer.switchServer(u.player, Network.getInstance().getGlobalSQL().getString("SELECT name FROM server_data WHERE type='LOBBY';"));

                    }

                });


    }

    //This methods is not needed in this class, so it is empty.
    @Override
    public void refresh() {
    }
}
