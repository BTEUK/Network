package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.UserUpdate;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Time;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.LOGGER;

public class Afk extends AbstractCommand {

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        NetworkUser user = Network.getInstance().getUser(player);

        //If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + player.getName() + " can not be found!");
            player.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        //Switch afk status.
        if (user.afk) {
            //Reset last logged time.
            user.last_movement = Time.currentTime();
            user.afk = false;
            updateAfkStatus(user, false);
        } else {
            user.afk = true;
            updateAfkStatus(user, true);
        }
    }

    public static void updateAfkStatus(NetworkUser user, boolean afk) {

        // Broadcast the afk message and send a user update event.
        Network.getInstance().getChat().broadcastAFK(user.player, afk);

        UserUpdate userUpdateEvent = new UserUpdate();
        userUpdateEvent.setUuid(user.player.getUniqueId().toString());
        userUpdateEvent.setAfk(afk);
        Network.getInstance().getChat().sendSocketMesage(userUpdateEvent);

    }
}
