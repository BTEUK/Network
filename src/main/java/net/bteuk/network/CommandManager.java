package net.bteuk.network;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.bteuk.network.commands.Discord;
import net.bteuk.network.commands.Help;
import net.bteuk.network.commands.Navigator;
import net.bteuk.network.commands.Nightvision;
import net.bteuk.network.commands.Plot;
import net.bteuk.network.commands.RegionCommand;
import net.bteuk.network.commands.Speed;
import net.bteuk.network.commands.Where;
import net.bteuk.network.commands.navigation.Back;
import net.bteuk.network.commands.navigation.Tp;
import net.bteuk.network.commands.navigation.TpToggle;
import net.bteuk.network.commands.navigation.Tpll;
import net.bteuk.network.commands.navigation.Warp;
import net.bteuk.network.commands.navigation.Warps;
import net.bteuk.network.commands.staff.Staff;
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

            /*
             * Gui commands.
             */
            commands.register("navigator", "Opens the main gui, will always return to the previous menu if possible.", List.of("nav", "gui", "menu", "claim"), new Navigator());
            commands.register("plot", "Allows players to manipulate plots without using the gui.", List.of("plots"), new Plot(instance));
            commands.register("region", "Allows players to manipulate regions without using the gui.", new RegionCommand());

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
        });

    }
}
