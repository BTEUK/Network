package me.bteuk.network.events;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static me.bteuk.network.utils.Constants.LOGGER;

/**
 * Event for kicking players from the server
 *
 * A kick event does not have any additional arguments, but should include a message.
 */
public class KickEvent extends AbstractEvent {

    @Override
    public void event(String uuid, String[] args, String message) {

        //Check if player is online.
        for (Player p : Bukkit.getOnlinePlayers()) {

            if (p.getUniqueId().toString().equals(uuid)) {

                //Kick the player.
                //If message is null send default kick message.
                if (message == null) {
                    p.kick();
                } else {
                    p.kick(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
                }
                return;

            }
        }

        //If the player could not be found log it in the console.
        LOGGER.warning("Attempted to kick player with uuid " + uuid + " but they could not be found on this server!");
    }
}
