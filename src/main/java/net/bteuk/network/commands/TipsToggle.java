package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class TipsToggle extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        toggleTips(player);
    }

    public static void toggleTips(Player p) {

        //Get the NetworkUser for this player.
        NetworkUser user = Network.getInstance().getUser(p);

        if (user == null) {
            LOGGER.warning("NetworkUser for player " + p.getName() + " is null!");
            return;
        }

        //If tips is enabled, disable it, else enable.
        if (user.isTipsEnabled()) {
            //Disable tips.
            user.setTipsEnabled(false);
            p.sendMessage(ChatUtils.success("Disabled tips in chat."));
        } else {
            //Enable tips.
            user.setTipsEnabled(true);
            p.sendMessage(ChatUtils.success("Enabled tips in chat."));
        }
    }
}