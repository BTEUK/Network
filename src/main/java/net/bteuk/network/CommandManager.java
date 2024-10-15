package net.bteuk.network;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.bteuk.network.commands.Afk;
import net.bteuk.network.commands.Clear;
import net.bteuk.network.commands.Discord;
import net.bteuk.network.commands.Gamemode;
import net.bteuk.network.commands.Help;
import net.bteuk.network.commands.Navigator;
import net.bteuk.network.commands.Nightvision;
import net.bteuk.network.commands.Phead;
import net.bteuk.network.commands.Plot;
import net.bteuk.network.commands.RegionCommand;
import net.bteuk.network.commands.Rules;
import net.bteuk.network.commands.Speed;
import net.bteuk.network.commands.Where;
import net.bteuk.network.commands.Zone;
import net.bteuk.network.commands.give.GiveBarrier;
import net.bteuk.network.commands.give.GiveDebugStick;
import net.bteuk.network.commands.give.GiveLight;
import net.bteuk.network.commands.navigation.Back;
import net.bteuk.network.commands.navigation.Delhome;
import net.bteuk.network.commands.navigation.Home;
import net.bteuk.network.commands.navigation.Homes;
import net.bteuk.network.commands.navigation.Navigation;
import net.bteuk.network.commands.navigation.Server;
import net.bteuk.network.commands.navigation.Sethome;
import net.bteuk.network.commands.navigation.Spawn;
import net.bteuk.network.commands.navigation.Tp;
import net.bteuk.network.commands.navigation.TpToggle;
import net.bteuk.network.commands.navigation.Tpll;
import net.bteuk.network.commands.navigation.Warp;
import net.bteuk.network.commands.navigation.Warps;
import net.bteuk.network.commands.staff.Staff;
import net.bteuk.network.lobby.LobbyCommand;
import net.buildtheearth.terraminusminus.TerraConfig;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static net.bteuk.network.utils.Constants.LL;
import static net.bteuk.network.utils.Constants.TPLL_ENABLED;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class CommandManager {

    public static void registerCommands(Network instance) {

        LifecycleEventManager<Plugin> manager = instance.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            /*
             * Navigation commands.
             */
            if (TPLL_ENABLED) {
                TerraConfig.reducedConsoleMessages = true;
                commands.register("tpll", "Teleport to coordinates", new Tpll(instance, CONFIG.getBoolean("requires_permission")));
            }
            if (LL) {
                commands.register("where", "Returns the coordinates where the player is standing with a link to google maps.", List.of("location", "ll"), new Where(instance));
            }
            commands.register("teleport", "Teleport to any online player.", List.of("tp"), new Tp());
            commands.register("back", "Teleports the player to the previous teleported location.", new Back());
            commands.register("warp", "Warp to locations in the exploration menu.", new Warp());
            commands.register("warps", "List all warps on the server, 16 per page.", new Warps());
            commands.register("navigation", "Adds commands to do with navigation.", new Navigation());
            commands.register("lobby", "Command for all lobby management.", new LobbyCommand(instance));
            if (CONFIG.getBoolean("homes.enabled")) {
                commands.register("sethome", "Set a home to your current location.", new Sethome(instance));
                commands.register("home", "Teleport to your home.", new Home(instance));
                commands.register("delhome", "Delete a home.", new Delhome(instance));
                commands.register("homes", "Like warps, but for homes, shows all homes the player has set.", new Homes());
            }
            commands.register("spawn", "Teleport to spawnpoint in lobby.", new Spawn());
            commands.register("server", "Switch server by command.", new Server());

            /*
             * Gui commands.
             */
            commands.register("navigator", "Opens the main gui, will always return to the previous menu if possible.", List.of("nav", "gui", "menu", "claim"), new Navigator());
            commands.register("plot", "Allows players to manipulate plots without using the gui.", List.of("plots"), new Plot(instance));
            commands.register("region", "Allows players to manipulate regions without using the gui.", new RegionCommand());
            commands.register("zone", "Zone command.", new Zone());

            /*
             * Staff commands.
             */
            commands.register("staff", "Opens the Staff Menu.", List.of("st"), new Staff());

            /*
             * Utility commands.
             */
            commands.register("teleporttoggle", "Enables/Disables the ability for other players to teleport to you.", List.of("tptoggle", "toggleteleport", "toggletp"), new TpToggle());
            commands.register("discord", "Sends a link to our discord server.", new Discord());
            commands.register("nightvision", "Toggle nightvision.", List.of("nv"), new Nightvision());
            commands.register("speed", "Sets the players speed, value up to 10.", new Speed());
            commands.register("help", "Help menu for information on commands and server features.", new Help());
            commands.register("afk", "Toggles afk status.", new Afk());
            commands.register("rules", "Get rules book.", new Rules());
            commands.register("clear", "Clears your inventory.", new Clear());
            commands.register("debugstick", "Get the debug stick.", new GiveDebugStick());
            commands.register("light", "Get a light block.", new GiveLight());
            commands.register("barrier", "Get a barrier block.", new GiveBarrier());
            commands.register("gamemode", "Switch gamemode.", List.of("gm"), new Gamemode());
            commands.register("phead", "Get the player head of someone who has connected to the server.", new Phead());
        });

    }
}
