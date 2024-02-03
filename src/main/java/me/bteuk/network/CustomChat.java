package me.bteuk.network;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.bteuk.network.exceptions.NotMutedException;
import me.bteuk.network.utils.staff.Moderation;
import me.bteuk.network.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import static me.bteuk.network.utils.Constants.*;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public class CustomChat extends Moderation implements Listener, PluginMessageListener {

    private final Network instance;
    private String IP;
    private int port;

    public CustomChat(Network instance) {

        this.instance = instance;

        instance.getServer().getPluginManager().registerEvents(this, instance);

        //If global chat is enabled, register the listeners.
        if (GLOBAL_CHAT) {

            this.IP = CONFIG.getString("chat.global_chat.socket.IP");
            this.port = CONFIG.getInt("chat.global_chat.socket.port");

            //Register chat channels.
            instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:globalchat", this);
            instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:reviewer", this);
            instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:staff", this);
            instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:connect", this);
            instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:disconnect", this);

            if (TAB) {
                //Register custom tab.
                instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:tab", this);
            }

            if (DISCORD_CHAT) {
                //Discord specific channels.
                instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:discord_chat", this);
                instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:discord_announcements", this);
                instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:discord_staff", this);
                instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:discord_reviewer", this);
            }

            if (DISCORD_LINKING) {
                instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:discord_linking", this);
            }

            LOGGER.info("Successfully enabled Global Chat!");
        }
    }

    public void onDisable() {

        if (GLOBAL_CHAT) {
            instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:globalchat");
            instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:reviewer");
            instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:staff");
            instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:connect");
            instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:disconnect");

            if (TAB) {
                instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:tab");
            }

            if (DISCORD_CHAT) {
                //Discord specific channels.
                instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:discord_chat", this);
                instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:discord_announcements", this);
                instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:discord_staff", this);
                instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:discord_reviewer", this);
            }

            if (DISCORD_LINKING) {
                instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:discord_linking", this);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(AsyncChatEvent e) {

        //If player is muted cancel.
        if (isMuted(e.getPlayer().getUniqueId().toString())) {
            e.setCancelled(true);
            try {

                //Send message and end event.
                e.getPlayer().sendMessage(getMutedComponent(e.getPlayer().getUniqueId().toString()));
                return;

            } catch (NotMutedException ex) {

                //Unset the muted status.
                e.setCancelled(false);

            }
        }

        if (!e.isCancelled()) {
            e.setCancelled(true);
            //Get user, if staff chat enabled send message to staff chat.
            NetworkUser u = instance.getUser(e.getPlayer());

            //If u is null, cancel.
            if (u == null) {
                LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
                e.getPlayer().sendMessage(Utils.error("User can not be found, please relog!"));
                return;
            }

            //Reset last movement of player, if they're afk unset that.
            u.last_movement = Time.currentTime();

            if (u.afk) {
                u.last_time_log = u.last_movement;
                u.afk = false;
                broadcastAFK(u.player, false);
            }

            Statistics.addMessage(e.getPlayer().getUniqueId().toString(), Time.getDate(Time.currentTime()));

            if (STAFF_CHAT && u.staffChat) {
                broadcastPlayerMessage(e.getPlayer(), e.message().color(NamedTextColor.WHITE), "uknet:staff");

                if (DISCORD_CHAT) {
                    broadcastPlayerMessage(e.getPlayer(), e.message().color(NamedTextColor.WHITE), "uknet:discord_staff");
                }
            } else {
                broadcastPlayerMessage(e.getPlayer(), e.message().color(NamedTextColor.WHITE), "uknet:globalchat");

                if (DISCORD_CHAT) {
                    broadcastPlayerMessage(e.getPlayer(), e.message().color(NamedTextColor.WHITE), "uknet:discord_chat");
                }
            }
        }
    }

    //Receives a message from the chat socket in json format.
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        String sMessage;
        try {
            sMessage = in.readUTF();
        } catch (IOException e) {
            LOGGER.severe("Could not broadcast received socket message!");
            return;
        }

        Component component = GsonComponentSerializer.gson().deserialize(sMessage);

        sendReceivedMessage(component, channel);

    }

    private void sendReceivedMessage(Component component, String channel) {
        switch (channel) {
            case "uknet:globalchat", "uknet:connect", "uknet:disconnect" -> Bukkit.getServer().broadcast(component);
            case "uknet:reviewer" -> {

                //Send only to reviewers.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("group.reviewer")) {
                        p.sendMessage(component);
                    }
                }
            }
            case "uknet:staff" -> {

                //Send only to staff.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("uknet.staff")) {
                        p.sendMessage(Component.text("[Staff]", NamedTextColor.RED).append(component));
                    }
                }
            }
            case "uknet:discord_linking" -> {

                //This is for account linking.
                String plainMessage = PlainTextComponentSerializer.plainText().serialize(component);
                String[] args = plainMessage.split(" ");
                if (args[0].equalsIgnoreCase("link")) {

                    //Check if player is online.
                    Player p = Bukkit.getPlayer(UUID.fromString(args[1]));
                    if (!(p == null)) {
                        if (p.isOnline()) {

                            //Link account.
                            instance.globalSQL.update("INSERT INTO discord(uuid,discord_id) VALUES('" + args[1] + "','" + args[2] + "');");
                            NetworkUser u = Network.getInstance().getUser(p);

                            //If u is null, cancel.
                            if (u == null) {
                                LOGGER.severe("User " + p.getName() + " can not be found!");
                                p.sendMessage(Utils.error("User can not be found, please relog!"));
                                return;
                            }

                            u.isLinked = true;
                            u.discord_id = Long.parseLong(args[2]);

                            //Get the highest role for syncing and sync it, except for guest.
                            String role = Roles.builderRole(p);

                            //Remove all roles except current role.
                            for (Map.Entry<String, Long> entry : Network.getInstance().getTimers().getRoles().entrySet()) {

                                if (role.equals(entry.getKey())) {
                                    broadcastMessage(Component.text("addrole " + args[2] + " " + entry.getValue()), "uknet:discord_linking");
                                } else {
                                    broadcastMessage(Component.text("removerole " + args[2] + " " + entry.getValue()), "uknet:discord_linking");
                                }

                            }

                            p.sendMessage(Utils.success("Your discord has been linked!"));

                        }
                    }
                } else if (args[0].equalsIgnoreCase("unlink")) {
                    //The user may still be in the linked here, while they are no longer in the discord, try removing the link.
                    try {
                        long discord_id = Long.parseLong(args[1]);
                        for (NetworkUser u : instance.getUsers()) {
                            if (u.isLinked && u.discord_id == discord_id) {
                                // Unlink
                                u.isLinked = false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.severe("An unlink event was sent with a non-long discord id: " + plainMessage);
                    }
                }
            }
            case "uknet:tab" ->

                //Run a tab update, the structure is the following.
                //'update/add/remove <uuid>'
                //This allows us to only update what is necessary.
                //Make sure it runs after all other scheduled tasks, so it does not front-run the disconnect event if the player just left this server.
                    Bukkit.getScheduler().runTask(instance, () -> Network.getInstance().tab.updateAll(PlainTextComponentSerializer.plainText().serialize(component)));
        }
    }

    public void broadcastPlayerMessage(Player player, Component component, String channel) {

        //Add formatted player name to start of the message.
        //Create a new component and append the component to that.
        Component message = Utils.chatFormat(player, component);

        //If global chat is enabled send the message to the proxy using the chat socket.
        if (GLOBAL_CHAT) {
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                try (Socket socket = new Socket(IP, port)) {

                    //Convert component to json and write to output.
                    OutputStream output = socket.getOutputStream();
                    ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                    objectOutput.writeObject(GsonComponentSerializer.gson().serialize(message));
                    objectOutput.writeObject(channel);
                    objectOutput.flush();

                    objectOutput.close();
                } catch (IOException ex) {
                    LOGGER.severe("Could not broadcast message to server socket!");
                }
            });
        } else {

            //Send locally.
            //Check for valid channels.
            sendReceivedMessage(message, channel);

        }
    }

    public void broadcastMessage(Component message, String channel) {

        //If global chat is enabled send the message to the proxy using the chat socket.
        if (GLOBAL_CHAT) {

            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                try (Socket socket = new Socket(IP, port)) {

                    //Convert component to json and write to output.
                    OutputStream output = socket.getOutputStream();
                    ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                    // Send player message
                    objectOutput.writeObject(Utils.toJson(message));
                    objectOutput.writeObject(channel);
                    objectOutput.flush();

                    objectOutput.close();
                } catch (IOException ex) {
                    LOGGER.severe("Could not broadcast message to server socket!");
                }
            });
        } else {

            //Send locally.
            //Check for valid channels.
            sendReceivedMessage(message, channel);

        }
    }

    //Send afk or no longer afk message to players ingame and discord.
    public void broadcastAFK(Player p, boolean afk) {

        Component message;

        if (afk) {
            message = Component.text(p.getName() + " is now afk", NamedTextColor.GRAY);
        } else {
            message = Component.text(p.getName() + " is no longer afk", NamedTextColor.GRAY);
        }

        //Send message
        broadcastMessage(message, "uknet:globalchat");

        if (DISCORD_CHAT) {

            broadcastDiscordAnnouncement(message, "afk");

        }
    }

    /**
     * Broadcasts a message to chat and discord (if enabled).
     *
     * @param message the message to broadcast
     */
    public void broadcastToChatAndDiscord(Component message) {

        //Send message
        broadcastMessage(message, "uknet:globalchat");

        if (DISCORD_CHAT) {

            broadcastMessage(message, "uknet:discord_chat");

        }
    }

    /**
     * Broadcasts a discord announcement. The announcement type is used to decide how to use/format the message.
     *
     * @param message the message to announce
     * @param announcement_type the type of announcement
     */
    public void broadcastDiscordAnnouncement(Component message, String announcement_type) {
        broadcastMessage(Component.text(announcement_type).append(Component.text(" ").append(message)), "uknet:discord_announcements");
    }
}
