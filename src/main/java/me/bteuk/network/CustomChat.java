package me.bteuk.network;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.bteuk.network.staff.Moderation;
import me.bteuk.network.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
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

import static me.bteuk.network.utils.Constants.LOGGER;

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
        instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:tab", this);

        LOGGER.info("Successfully enabled Global Chat!");

    }

    public void onDisable() {

        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:globalchat");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:reviewer");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:staff");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:connect");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:disconnect");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:discord");
        instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:tab");

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(AsyncChatEvent e) {

        //If player is muted cancel.
        if (moderation.isMuted(e.getPlayer().getUniqueId().toString())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("&cYou are muted for &4" +
                    moderation.getMutedReason(e.getPlayer().getUniqueId() + " &cuntil &4" +
                            moderation.getMuteDuration(e.getPlayer().getUniqueId().toString())));
        }

        if (!e.isCancelled()) {
            e.setCancelled(true);
            //Get user, if staff chat enabled send message to staff chat.
            NetworkUser u = instance.getUser(e.getPlayer());

            //If u is null, cancel.
            if (u == null) {
                LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
                e.getPlayer().sendMessage("User can not be found, please relog!");
                return;
            }

            //Reset last movement of player, if they're afk unset that.
            u.last_movement = Time.currentTime();

            if (u.afk) {
                u.last_time_log = u.last_movement;
                u.afk = false;
                Network.getInstance().chat.broadcastMessage(Component.text(u.player.getName() + " is no longer afk.", NamedTextColor.GRAY), "uknet:globalchat");
            }

            if (u.staffChat) {
                broadcastPlayerMessage(e.getPlayer(), e.message(), "uknet:staff");
            } else {
                Statistics.addMessage(e.getPlayer().getUniqueId().toString(), Time.getDate(Time.currentTime()));
                broadcastPlayerMessage(e.getPlayer(), e.message(), "uknet:globalchat");
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

        switch (channel) {

            case "uknet:globalchat":
            case "uknet:connect":
            case "uknet:disconnect":

                Bukkit.getServer().broadcast(component);
                break;

            case "uknet:reviewer":

                //Send only to reviewers.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("group.reviewer")) {
                        p.sendMessage(component);
                    }
                }
                break;

            case "uknet:staff":

                //Send only to staff.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("uknet.staff")) {
                        p.sendMessage(Component.text("[Staff]", NamedTextColor.RED).append(component));
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

                            //If u is null, cancel.
                            if (u == null) {
                                LOGGER.severe("User " + p.getName() + " can not be found!");
                                p.sendMessage("User can not be found, please relog!");
                                return;
                            }

                            u.isLinked = true;
                            u.discord_id = Long.parseLong(args[2]);

                            //Get the highest role for syncing and sync it, except for guest.
                            String role = Roles.builderRole(p);

                            //Remove all roles except current role.
                            for (Map.Entry<String, Long> entry : Network.getInstance().timers.getRoles().entrySet()) {

                                if (role.equals(entry.getKey())) {
                                    broadcastMessage(Component.text("addrole " + args[2] + " " + entry.getValue()), "uknet:discord");
                                } else {
                                    broadcastMessage(Component.text("removerole " + args[2] + " " + entry.getValue()), "uknet:discord");
                                }

                            }

                            p.sendMessage(Utils.success("Your discord has been linked!"));

                        }
                    }
                }

                break;

            case "uknet:tab":

                //Run a tab update, the structure is the following.
                //'update/add/remove <uuid>'
                //This allows us to only update what is necessary.
                Network.getInstance().tab.updateAll(sMessage);


        }
    }

    public void broadcastPlayerMessage(Player player, Component component, String channel) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try (Socket socket = new Socket(IP, port)) {

                //Add formatted player name to start of the message.
                //Create a new component and append the component to that.
                Component message = Utils.chatFormat(player, component);

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
    }

    public void broadcastMessage(Component message, String channel) {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            try (Socket socket = new Socket(instance.socketIP, instance.socketPort)) {

                //Convert component to json and write to output.
                OutputStream output = socket.getOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                // Send player message
                objectOutput.writeObject(GsonComponentSerializer.gson().serialize(message));
                objectOutput.writeObject(channel);
                objectOutput.flush();

                objectOutput.close();
            } catch (IOException ex) {
                LOGGER.severe("Could not broadcast message to server socket!");
            }
        });
    }
}
