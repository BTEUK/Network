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
import net.bteuk.network.lib.dto.UserRemove;
import net.bteuk.network.lib.socket.OutputSocket;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Role;
import net.bteuk.network.utils.Roles;
import net.bteuk.network.utils.Time;
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

import static net.bteuk.network.commands.AFK.updateAfkStatus;
import static net.bteuk.network.lib.enums.ChatChannels.STAFF;
import static net.bteuk.network.utils.Constants.GLOBAL_CHAT;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;
import static net.bteuk.network.utils.staff.Moderation.getMutedComponent;
import static net.bteuk.network.utils.staff.Moderation.isMuted;

public class CustomChat implements Listener, PluginMessageListener {

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
            NetworkUser user = instance.getUser(e.getPlayer());

            //If u is null, cancel.
            if (user == null) {
                LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
                e.getPlayer().sendMessage(ChatUtils.error("User can not be found, please relog!"));
                return;
            }

            //Reset last movement of player, if they're afk unset that.
            user.last_movement = Time.currentTime();

            if (user.afk) {
                user.afk = false;
                updateAfkStatus(user, false);
            }
            ChatMessage chatMessage = getChatMessage(e.message(), user);
            sendSocketMesage(chatMessage);
        }
    }

    public void sendSocketMesage(AbstractTransferObject chatMessage) {
        outputSocket.sendSocketMessage(chatMessage);
    }

    public static ChatMessage getChatMessage(Component component, NetworkUser u) {

        ChatMessage chatMessage = new ChatMessage();
        Component message = playerMessageFormat(u, component);

        if (u.getChatChannel().equals(STAFF.getChannelName())) {
            message = Component.text("[Staff]", NamedTextColor.RED).append(message);
            // Prefix the chat message with [Staff]
        }

        chatMessage.setChannel(u.getChatChannel());
        chatMessage.setSender(u.player.getUniqueId().toString());
        chatMessage.setComponent(message);
        return chatMessage;
    }

    public static DirectMessage getDirectMessage(String message, String senderName, String senderUuid, String recipientName, String recipientUuid) {
        return new DirectMessage(recipientUuid, senderUuid, directMessageFormat(message, senderName, recipientName), false);
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
            } else if (object instanceof UserRemove userRemove) {
                Connect.handleUserRemove(userRemove);
            }

        } catch (IOException e) {
            e.printStackTrace();
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
                if (u.isLinked && u.getDiscordId() == discordLinking.getDiscordId()) {
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
                    user.setDiscordId(discordLinking.getDiscordId());

                    // Get the highest role for syncing and sync it, except for guest.
                    Role role = Roles.builderRole(user.player);

                    // Add the role in discord.
                    if (role == null) {
                        user.sendMessage(ChatUtils.error("You have an invalid role, please contact an administrator."));
                        return;
                    }

                    DiscordRole discordRole = new DiscordRole(user.player.getUniqueId().toString(), role.getId(), true);
                    outputSocket.sendSocketMessage(discordRole);

                    user.sendMessage(ChatUtils.success("Your discord has been linked!"));

                });
    }

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

    /**
     * Format a player message to add the player prefix and name.
     *
     * @param message the {@link Component} to format
     * @return the {@link Component} formatted message
     */
    private static Component playerMessageFormat(NetworkUser user, Component message) {
        Role userRole = user.getPrimaryRole();
        return userRole.getColouredPrefix() // The prefix based on the role.
                .append(Component.space())
                .append(ChatUtils.line(user.player.getName())) // Player name in white without formatting.
                .append(Component.space())
                .append(ChatUtils.line(">").decorate(TextDecoration.BOLD)) // Arrow between the player and message in bold.
                .append(Component.space())
                .append(message.color(NamedTextColor.WHITE)); // The message in white without formatting.
    }

    private static Component directMessageFormat(String message, String sender, String recipient) {
        return ChatUtils.line("[").decorate(TextDecoration.BOLD)
                .append(ChatUtils.line(sender))
                .append(Component.space())
                .append(ChatUtils.line("->"))
                .append(Component.space())
                .append(ChatUtils.line(recipient))
                .append(ChatUtils.line("]").decorate(TextDecoration.BOLD))
                .append(Component.space())
                .append(ChatUtils.line(">").decorate(TextDecoration.BOLD)) // Arrow between the player and message in bold.
                .append(Component.space())
                .append(ChatUtils.line(message)); // The message in white without formatting.
    }
}
