package net.bteuk.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.bteuk.network.eventing.listeners.Connect;
import net.bteuk.network.exceptions.NotMutedException;
import net.bteuk.network.lib.dto.AbstractTransferObject;
import net.bteuk.network.lib.dto.AddTeamEvent;
import net.bteuk.network.lib.dto.ChatMessage;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.dto.DiscordLinking;
import net.bteuk.network.lib.dto.DiscordRole;
import net.bteuk.network.lib.dto.UserConnectReply;
import net.bteuk.network.lib.socket.OutputSocket;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Roles;
import net.bteuk.network.utils.Statistics;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.staff.Moderation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.bteuk.network.utils.Constants.GLOBAL_CHAT;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.STAFF_CHAT;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class CustomChat extends Moderation implements Listener, PluginMessageListener {

    private final Network instance;

    private OutputSocket outputSocket;

    private static final String AFK = "%s is now afk";
    private static final String NOT_AFK = "%s is no longer afk";

    public CustomChat(Network instance) {

        this.instance = instance;

        instance.getServer().getPluginManager().registerEvents(this, instance);

        //If global chat is enabled, register the listeners.
        if (GLOBAL_CHAT) {

            // Setup the output socket.
            outputSocket = new OutputSocket(
                    CONFIG.getString("chat.global_chat.socket.IP"),
                    CONFIG.getInt("chat.global_chat.socket.port")
            );

            //Register channel for messages received from the proxy.
            instance.getServer().getMessenger().registerIncomingPluginChannel(instance, "uknet:network", this);

            LOGGER.info("Successfully enabled Global Chat!");
        }
    }

    public void onDisable() {
        if (GLOBAL_CHAT) {
            instance.getServer().getMessenger().unregisterIncomingPluginChannel(instance, "uknet:network");
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
                e.getPlayer().sendMessage(ChatUtils.error("User can not be found, please relog!"));
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
            ChatMessage chatMessage = getChatMessage(e.message(), u);

            sendSocketMesage(chatMessage);
        }
    }

    public void sendSocketMesage(AbstractTransferObject chatMessage) {
        outputSocket.sendSocketMessage(chatMessage);
    }

    public static ChatMessage getChatMessage(Component component, NetworkUser u) {

        ChatMessage chatMessage = new ChatMessage();
        Component message = component.color(NamedTextColor.WHITE);

        if (STAFF_CHAT && u.getChatChannel().equals("staff")) {
            chatMessage.setChannel("staff");
            message = Component.text("[Staff]", NamedTextColor.RED).append(message);
            // Prefix the chat message with [Staff]
        } else if (u.getChatChannel().equals("global")) {
            chatMessage.setChannel("global");
        } else {
            // A private channel was used, add the correct prefix to the message.

        }

        chatMessage.setSender(u.player.getUniqueId().toString());
        chatMessage.setComponent(message);
        return chatMessage;
    }

    //Receives a message from the chat socket in json format.
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            AbstractTransferObject object = mapper.readValue(message, AbstractTransferObject.class);

            if (object instanceof ChatMessage chatMessage) {
                handleChatMessage(chatMessage);
            } else if (object instanceof DirectMessage directMessage) {
                handleDirectMessage(directMessage);
            } else if (object instanceof DiscordLinking discordLinking) {
                handleDiscordLinking(discordLinking);
            } else if (object instanceof AddTeamEvent addTeamEvent) {
                instance.getTab().handle(addTeamEvent);
            } else if (object instanceof UserConnectReply userConnectReply) {
                Connect.handleUserConnectReply(userConnectReply);
            }

        } catch (IOException e) {
            // Ignored
            LOGGER.severe("An error occurred while receiving message from proxy.");
        }
    }

    private void handleChatMessage(ChatMessage message) {

        switch (message.getChannel()) {

            case "global" -> Bukkit.getServer().broadcast(message.getComponent());

            case "staff" -> {
                // Send only to staff.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("uknet.staff")) {
                        p.sendMessage(message.getComponent());
                    }
                }
            }

            case "reviewer" -> {

                // Send only to reviewers.
                for (Player p : instance.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("group.reviewer")) {
                        p.sendMessage(message.getComponent());
                    }
                }
            }
        }

    }

    private void handleDirectMessage(DirectMessage message) {

        // Send the message if the player is on this server.
        instance.getServer().getOnlinePlayers().stream()
                .filter(player -> player.getUniqueId().toString().equals(message.getRecipient()))
                .forEach(player -> player.sendMessage(message.getComponent()));

    }

    private void handleDiscordLinking(DiscordLinking discordLinking) {

        if (discordLinking.isUnlink() && discordLinking.getDiscordId() != -1) {
            // Unlink
            for (NetworkUser u : instance.getUsers()) {
                if (u.isLinked && u.discord_id == discordLinking.getDiscordId()) {
                    // Unlink
                    u.isLinked = false;
                }
            }
            return;
        }

        if (discordLinking.getUuid() == null || discordLinking.isUnlink() || discordLinking.getDiscordId() == -1) {
            return;
        }

        // Find the user.
        instance.getUsers().stream()
                .filter(user -> user.player.getUniqueId().toString().equals(discordLinking.getUuid()))
                .forEach(user -> {

                    // Link account.
                    instance.getGlobalSQL().update("INSERT INTO discord(uuid,discord_id) VALUES('" + discordLinking.getUuid() + "'," + discordLinking.getDiscordId() + ");");

                    user.isLinked = true;
                    user.discord_id = discordLinking.getDiscordId();

                    // Get the highest role for syncing and sync it, except for guest.
                    String role = Roles.builderRole(user.player);

                    // Add the role in discord.
                    DiscordRole discordRole = new DiscordRole(user.player.getUniqueId().toString(), role, true);
                    outputSocket.sendSocketMessage(discordRole);

                    user.sendMessage(ChatUtils.success("Your discord has been linked!"));

                });
    }

//    private void sendReceivedMessage(Component component, String channel) {
//        switch (channel) {
//            case "uknet:discord_linking" -> {
//
//                //This is for account linking.
//                String plainMessage = PlainTextComponentSerializer.plainText().serialize(component);
//                String[] args = plainMessage.split(" ");
//                if (args[0].equalsIgnoreCase("link")) {
//
//                    //Check if player is online.
//                    Player p = Bukkit.getPlayer(UUID.fromString(args[1]));
//                    if (!(p == null)) {
//                        if (p.isOnline()) {
//
//                            //Link account.
//                            instance.getGlobalSQL().update("INSERT INTO discord(uuid,discord_id) VALUES('" + args[1] + "','" + args[2] + "');");
//                            NetworkUser u = Network.getInstance().getUser(p);
//
//                            //If u is null, cancel.
//                            if (u == null) {
//                                LOGGER.severe("User " + p.getName() + " can not be found!");
//                                p.sendMessage(ChatUtils.error("User can not be found, please relog!"));
//                                return;
//                            }
//
//                            u.isLinked = true;
//                            u.discord_id = Long.parseLong(args[2]);
//
//                            //Get the highest role for syncing and sync it, except for guest.
//                            String role = Roles.builderRole(p);
//
//                            //Remove all roles except current role.
//                            for (Map.Entry<String, Long> entry : Network.getInstance().getTimers().getRoles().entrySet()) {
//
//                                if (role.equals(entry.getKey())) {
//                                    broadcastMessage(Component.text("addrole " + args[2] + " " + entry.getValue()), "uknet:discord_linking");
//                                } else {
//                                    broadcastMessage(Component.text("removerole " + args[2] + " " + entry.getValue()), "uknet:discord_linking");
//                                }
//
//                            }
//
//                            p.sendMessage(ChatUtils.success("Your discord has been linked!"));
//
//                        }
//                    }
//                }
//            }
//            case "uknet:tab" ->
//
//                //Run a tab update, the structure is the following.
//                //'update/add/remove <uuid>'
//                //This allows us to only update what is necessary.
//                //Make sure it runs after all other scheduled tasks, so it does not front-run the disconnect event if the player just left this server.
//                    Bukkit.getScheduler().runTask(instance, () -> Network.getInstance().tab.updateAll(PlainTextComponentSerializer.plainText().serialize(component)));
//        }
//    }

//    public void broadcastMessage(Component message, String channel) {
//
//        //If global chat is enabled send the message to the proxy using the chat socket.
//        if (GLOBAL_CHAT) {
//
//            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
//                try (Socket socket = new Socket(IP, port)) {
//
//                    //Convert component to json and write to output.
//                    OutputStream output = socket.getOutputStream();
//                    ObjectOutputStream objectOutput = new ObjectOutputStream(output);
//
//                    // Send player message
//                    objectOutput.writeObject(Utils.toJson(message));
//                    objectOutput.writeObject(channel);
//                    objectOutput.flush();
//
//                    objectOutput.close();
//                } catch (IOException ex) {
//                    LOGGER.severe("Could not broadcast message to server socket!");
//                }
//            });
//        } else {
//
//            //Send locally.
//            //Check for valid channels.
//            sendReceivedMessage(message, channel);
//
//        }
//    }

    // Send afk or no longer afk message to players ingame and discord.
    public void broadcastAFK(Player p, boolean afk) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(p.getUniqueId().toString());
        chatMessage.setChannel("global");

        if (afk) {
            chatMessage.setComponent(Component.text(String.format(AFK, p.getName()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
        } else {
            chatMessage.setComponent(Component.text(String.format(NOT_AFK, p.getName()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, true));
        }

        // Send message
        sendSocketMesage(chatMessage);

    }

//    /**
//     * Broadcasts a discord announcement. The announcement type is used to decide how to use/format the message.
//     *
//     * @param message           the message to announce
//     * @param announcement_type the type of announcement
//     */
//    public void broadcastDiscordAnnouncement(Component message, String announcement_type) {
//        broadcastMessage(Component.text(announcement_type).append(Component.text(" ").append(message)), "uknet:discord_announcements");
//    }
}
