package me.bteuk.network;

import me.bteuk.network.staff.Moderation;
import me.bteuk.network.utils.*;
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
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CustomChat implements Listener, PluginMessageListener {

    private final Network instance;
    private final String IP;
    private final int port;

    private final Moderation moderation;

    public CustomChat(Network instance, String IP, int port) {

        this.instance = instance;
        this.IP = IP;
        this.port = port;

        moderation = new Moderation();

        instance.getServer().getPluginManager().registerEvents(this, instance);

        //Register chat channels.
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:globalchat", this);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:reviewer", this);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:staff", this);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:connect", this);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:disconnect", this);
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:discord", this);

        instance.getLogger().info("Successfully enabled Global Chat!");

    }

    public void onDisable() {

        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:globalchat");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:reviewer");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:staff");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:connect");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:disconnect");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:discord");

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(AsyncPlayerChatEvent e) {

        //If player is muted cancel.
        if (moderation.isMuted(e.getPlayer().getUniqueId().toString())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("&cYou are muted for &4" +
                    moderation.getMutedReason(e.getPlayer().getUniqueId().toString() + " &cuntil &4" +
                            moderation.getMuteDuration(e.getPlayer().getUniqueId().toString())));
        }

        if (e.isCancelled()) {
            //If chat is already cancelled, don't broadcast.
        } else {
            e.setCancelled(true);
            //Get user, if staff chat enabled send message to staff chat.
            NetworkUser u = instance.getUser(e.getPlayer());
            if (u.staffChat) {
                broadcastPlayerMessage(e.getPlayer(), e.getMessage(), "uknet:staff");
            } else {
                Statistics.addMessage(e.getPlayer().getUniqueId().toString(), Time.getDate(Time.currentTime()));
                broadcastPlayerMessage(e.getPlayer(), e.getMessage(), "uknet:globalchat");
            }
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        String sMessage;
        try {
            sMessage = in.readUTF();
        } catch (IOException e) {
            instance.getLogger().log(Level.SEVERE, "Could not broadcast received socket message!", e);
            return;
        }

        switch (channel) {

            case "uknet:globalchat":
            case "uknet:connect":
            case "uknet:disconnect":

                Bukkit.broadcastMessage(Utils.chat(sMessage));
                break;

            case "uknet:reviewer":

                //Send only to reviewers.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("group.reviewer")) {
                        p.sendMessage(Utils.chat(sMessage));
                    }
                }
                break;

            case "uknet:staff":

                //Send only to staff.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("uknet.staff")) {
                        p.sendMessage(Utils.chat("&c[Staff]&r" + sMessage));
                    }
                }
                break;

            case "uknet:discord":

                //This is for account linking.
                String[] args = sMessage.split(" ");

                if (args[0].equalsIgnoreCase("link")) {

                    //Check if player is online.
                    Player p = Bukkit.getPlayer(UUID.fromString(args[1]));
                    if (!(p == null)) {
                        if (p.isOnline()) {

                            //Link account.
                            instance.globalSQL.update("INSERT INTO discord(uuid,discord_id) VALUES('" + args[1] + "','" + args[2] + "');");
                            NetworkUser u = Network.getInstance().getUser(p);
                            u.isLinked = true;
                            u.discord_id = Long.parseLong(args[2]);

                            //Get the highest role for syncing and sync it, except for guest.
                            String role = Roles.builderRole(p);

                            //Remove all roles except current role.
                            for (Map.Entry<String, Long> entry : Network.getInstance().timers.getRoles().entrySet()) {

                                if (role.equals(entry.getKey())) {
                                    broadcastMessage("addrole " + args[2] + " " + entry.getValue(), "uknet:discord");
                                } else {
                                    broadcastMessage("removerole " + args[2] + " " + entry.getValue(), "uknet:discord");
                                }

                            }

                            p.sendMessage(Utils.success("Your discord has been linked!"));

                        }
                    }
                }

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
