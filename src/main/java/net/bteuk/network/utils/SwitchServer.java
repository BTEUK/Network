package net.bteuk.network.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.SwitchServerEvent;
import net.bteuk.network.lib.dto.UserDisconnect;
import net.bteuk.network.lib.utils.ChatUtils;
import org.bukkit.entity.Player;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class SwitchServer {

    public static void switchServer(Player p, String server) {

        NetworkUser user = Network.getInstance().getUser(p);

        // If u is null, cancel.
        if (user == null) {
            LOGGER.severe("User " + p.getName() + " can not be found!");
            p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
            return;
        }

        // If server is null, cancel and notify player.
        if (server == null) {
            p.sendMessage(ChatUtils.error("An error occured, server does not exist."));
            Network.getInstance().getLogger().warning("Player attempting to switch to non-existing server.");

            // Remove any join events that the player may have.
            Network.getInstance().getGlobalSQL().update("DELETE FROM join_events WHERE uuid='" + p.getUniqueId() +
                    "';");
            return;
        }

        // Check if server exists and is online.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT name FROM server_data WHERE name='" + server + "';")) {
            p.sendMessage(ChatUtils.error("The server " + server + " does not exist."));

            // Remove any join events that the player may have.
            Network.getInstance().getGlobalSQL().update("DELETE FROM join_events WHERE uuid='" + p.getUniqueId() +
                    "';");
            return;
        } else if (Network.getInstance().getGlobalSQL()
                .hasRow("SELECT online FROM server_data WHERE name='" + server + "' AND online=0;")) {
            p.sendMessage(ChatUtils.error("The server " + server + " is currently offline."));

            // Remove any join events that the player may have.
            Network.getInstance().getGlobalSQL().update("DELETE FROM join_events WHERE uuid='" + p.getUniqueId() +
                    "';");
            return;
        }

        // Set switching to true in user.
        user.switching = true;

        // Send switch server event to the proxy.
        UserDisconnect userDisconnect = user.createDisconnectEvent();
        SwitchServerEvent switchServerEvent = new SwitchServerEvent(p.getUniqueId().toString(), server, SERVER_NAME,
                userDisconnect);
        Network.getInstance().getChat().sendSocketMesage(switchServerEvent);
    }

    public static void switchToExternalServer(Player player) {

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        PacketContainer serverTransfer = new PacketContainer(PacketType.Play.Server.TRANSFER);
        serverTransfer.getStrings()
                .write(0, "bteuk.net");
        serverTransfer.getIntegers()
                .write(0, 25565);

        protocolManager.sendServerPacket(player, serverTransfer);
    }
}
