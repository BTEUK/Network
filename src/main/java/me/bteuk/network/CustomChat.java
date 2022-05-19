package me.bteuk.network;

import me.bteuk.network.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;

public class CustomChat implements Listener, PluginMessageListener {

    private Network instance;

    public CustomChat(Network instance) {

        this.instance = instance;

        instance.getServer().getPluginManager().registerEvents(this, instance);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:globalchat", this);

        instance.getLogger().info(Utils.chat("&aSuccessfully enabled Global Chat!"));

    }

    public void onDisable() {

        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:globalchat");

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(AsyncPlayerChatEvent e) {

        e.setFormat(getFormattedMessage(e.getPlayer(), e.getMessage()));
        broadcastPlayerMessage(e.getPlayer(), e.getMessage());

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Bukkit.broadcastMessage(Utils.chat("&e" + event.getPlayer().getName() + " joined UKnet"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Bukkit.broadcastMessage(Utils.chat("&e" + event.getPlayer().getName() + " left UKnet"));
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("uknet:globalchat")) {

            return;

        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        try {

            Bukkit.broadcastMessage(Utils.chat(in.readUTF()));

        } catch (IOException ex) {

            instance.getLogger().log(Level.SEVERE, Utils.chat("&cCould not broadcast received socket message!"), ex);

        }

    }

    public void broadcastPlayerMessage(Player player, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try (Socket socket = new Socket(instance.socketIP, instance.socketPort)) {
                OutputStream output = socket.getOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                // Send player message
                objectOutput.writeObject(getFormattedMessage(player, message));
                objectOutput.flush();

                objectOutput.close();
            } catch (IOException ex) {
                instance.getLogger().log(Level.SEVERE, "Could not broadcast message to server socket!", ex);
            }
        });
    }

    public void broadcastMessage(String message) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try (Socket socket = new Socket(instance.socketIP, instance.socketPort)) {
                OutputStream output = socket.getOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                // Send player message
                objectOutput.writeObject(message);
                objectOutput.flush();

                objectOutput.close();
            } catch (IOException ex) {
                instance.getLogger().log(Level.SEVERE, "Could not broadcast message to server socket!", ex);
            }
        });
    }

    public String getFormattedMessage(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, "§7] [%luckperms_prefix%§7] %player_name% &7&l> &7") + message;
    }
}
