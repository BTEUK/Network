package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.PlayerSelector;
import net.bteuk.network.lib.dto.MuteEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Implementation of pmute and punmute. The only difference between the 2 is the boolean that indicates whether it's a mute or unmute.
 */
public abstract class PmuteAction extends AbstractCommand {

    private final Component error;

    private final Network instance;

    protected PmuteAction(Network instance, String commandName, Component error) {
        super(instance, commandName);
        this.instance = instance;
        this.error = error;
        command.setTabCompleter(new PlayerSelector());
    }

    public void onCommand(CommandSender sender, String[] args, boolean mute) {

        Player p = getPlayer(sender);

        if (p == null) {
            return;
        }

        // Get the uuid of the player.
        if (args.length != 1) {
            p.sendMessage(error);
            return;
        }

        // Search for the uuid of the player.
        String uuid = instance.getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
        if (uuid == null) {
            p.sendMessage(error);
            return;
        }

        MuteEvent muteEvent = new MuteEvent(p.getUniqueId().toString(), uuid, mute);
        // Feedback will be sent through a direct message to the player by the proxy.
        instance.getChat().sendSocketMesage(muteEvent);
    }
}
