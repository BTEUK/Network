package me.bteuk.network;

import me.bteuk.network.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;

public class CustomChat implements Listener, PluginMessageListener {

    private final Network instance;
    private final String IP;
    private final int port;

    public CustomChat(Network instance, String IP, int port) {

        this.instance = instance;
        this.IP = IP;
        this.port = port;

        instance.getServer().getPluginManager().registerEvents(this, instance);

        //Register chat channels.
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:globalchat", this);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:reviewer", this);

        instance.getLogger().info("Successfully enabled Global Chat!");

    }

    public void onDisable() {

        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:globalchat");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:reviewer");

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(AsyncPlayerChatEvent e) {

        if (e.isCancelled()) {
            //If chat is already cancelled, don't broadcast.
        } else {
            e.setCancelled(true);
            broadcastPlayerMessage(e.getPlayer(), e.getMessage(), "uknet:globalchat");
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("uknet:globalchat") && !channel.equals("uknet:reviewer") && !channel.equals("uknet:connect") && !channel.equals("uknet:disconnect")) {

            return;

        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        try {

            if (channel.equals("uknet:reviewer")) {

                //Send only to reviewers.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("group.reviewer")) {
                        p.sendMessage(Utils.chat(in.readUTF()));
                    }
                }

            } else {

                Bukkit.broadcastMessage(Utils.chat(in.readUTF()));

            }

        } catch (IOException ex) {

            instance.getLogger().log(Level.SEVERE, "Could not broadcast received socket message!", ex);

        }

    }

    public void broadcastPlayerMessage(Player player, String message, String channel) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try (Socket socket = new Socket(IP, port)) {
                OutputStream output = socket.getOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                // Send player message
                objectOutput.writeObject(getFormattedMessage(player, message));
                objectOutput.writeObject(channel);
                objectOutput.flush();

                objectOutput.close();
            } catch (IOException ex) {
                instance.getLogger().log(Level.SEVERE, "Could not broadcast message to server socket!", ex);
            }
        });
    }

    public void broadcastMessage(String message, String channel) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try (Socket socket = new Socket(instance.socketIP, instance.socketPort)) {
                OutputStream output = socket.getOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                // Send player message
                objectOutput.writeObject(message);
                objectOutput.writeObject(channel);
                objectOutput.flush();

                objectOutput.close();
            } catch (IOException ex) {
                instance.getLogger().log(Level.SEVERE, "Could not broadcast message to server socket!", ex);
            }
        });
    }

    public String getFormattedMessage(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix% &f%player_name% &7&l> &r&f") + message;
    }
}
