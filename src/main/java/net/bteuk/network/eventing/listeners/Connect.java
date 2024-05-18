package net.bteuk.network.eventing.listeners;

import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.bteuk.network.Network;
import net.bteuk.network.building_companion.BuildingCompanion;
import net.bteuk.network.gui.Gui;
import net.bteuk.network.lib.dto.AbstractTransferObject;
import net.bteuk.network.lib.dto.UserConnectReply;
import net.bteuk.network.lib.dto.UserConnectRequest;
import net.bteuk.network.lib.dto.UserDisconnect;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.sql.RegionSQL;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Roles;
import net.bteuk.network.utils.Statistics;
import net.bteuk.network.utils.TextureUtils;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;

import static net.bteuk.network.utils.Constants.CUSTOM_MESSAGES;
import static net.bteuk.network.utils.Constants.DISCORD_CHAT;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.Constants.TAB;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

//This class deals with players joining and leaving the network.
public class Connect implements Listener {

    private final Network instance;

    private final GlobalSQL globalSQL;

    @Setter
    private boolean blockLeaveEvent;

    public Connect(Network instance, GlobalSQL globalSQL, PlotSQL plotSQL, RegionSQL regionSQL) {

        this.instance = instance;

        this.globalSQL = globalSQL;

        this.blockLeaveEvent = false;

        //Register join and leave events.
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void joinServerEvent(PlayerJoinEvent e) {

        // Block the default connect message, this will be sent by the proxy.
        e.joinMessage(null);

        // Determine the chat channels to which this user has access.
        Set<String> channels = NetworkUser.getChannels(e.getPlayer());

        // Fetch the user instance from the proxy.
        UserConnectRequest userConnectRequest = new UserConnectRequest(SERVER_NAME, e.getPlayer().getUniqueId().toString(), e.getPlayer().getName(), TextureUtils.getTexture(e.getPlayer().getPlayerProfile()), channels);

        // Run the call to the proxy async as to not freeze the server while waiting for a reply.
        Bukkit.getScheduler().runTaskAsynchronously(Network.getInstance(), () -> {

            AbstractTransferObject reply = Network.getInstance().getChat().sendSocketMesage(userConnectRequest);

            if (reply instanceof UserConnectReply userConnectReply) {
                Bukkit.getScheduler().runTask(Network.getInstance(), () -> handleUserConnectReply(e.getPlayer(), userConnectReply));
            } else {
                // This should not be possible.
                LOGGER.severe(String.format("Reply from user connect was of type %s, this should not be possible!", reply.getClass().getTypeName()));
            }
        });
    }

    private void handleUserConnectReply(Player player, UserConnectReply reply) {

        // Create the user instance.
        NetworkUser user = new NetworkUser(player, reply);

        // Add the user instance to the list.
        Network.getInstance().addUser(user);

    }

    @EventHandler
    public void leaveServerEvent(PlayerQuitEvent e) {

        e.quitMessage(null);

        if (blockLeaveEvent) {
            return;
        }

        NetworkUser u = instance.getUser(e.getPlayer());

        //If u is null, cancel.
        if (u == null) {
            LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
            e.getPlayer().sendMessage(Utils.error("User can not be found, please relog!"));
            return;
        }

        //Reset last logged time.
        if (u.afk) {
            u.last_time_log = u.last_movement = Time.currentTime();
            u.afk = false;
        }

        // If the companion is enabled, disable it.
        BuildingCompanion companion = u.getCompanion();
        if (companion != null) {
            companion.disable();
        }

        //Update statistics
        long time = Time.currentTime();
        Statistics.save(u, Time.getDate(time), time);

        //Remove user from list.
        instance.removeUser(u);

        //Get player uuid.
        UUID playerUUID = u.player.getUniqueId();

        //If they are currently in an inventory, remove them from the list of open inventories.
        Gui.openInventories.remove(playerUUID);

        //Delete any guis that may exist.
        if (u.mainGui != null) {
            u.mainGui.delete();
        }
        if (u.staffGui != null) {
            u.staffGui.delete();
        }
        if (u.lightsOut != null) {
            u.lightsOut.delete();
        }

        // Send a disconnect event to the proxy to handle potential messages.
        UserDisconnect userDisconnect = new UserDisconnect(u.player.getUniqueId().toString(), u.isNavigatorEnabled(), u.isTeleportEnabled(), u.isNightvisionEnabled(), u.getChatChannel(), u.isTips_enabled());
        Bukkit.getScheduler().runTaskAsynchronously(Network.getInstance(), () -> Network.getInstance().getChat().sendSocketMesage(userDisconnect));


        //If the player is not in the server_switch table they have disconnected from the network.
//        if (!globalSQL.hasRow("SELECT uuid FROM server_switch WHERE uuid='" + e.getPlayer().getUniqueId()
//                + "' AND from_server='" + SERVER_NAME + "';")) {
//
//            //Run leave network sequence.
//            networkLeaveEvent(e.getPlayer());
//            //Cancel default join message if custom messages are enabled.
//            if (CUSTOM_MESSAGES) {
//                e.quitMessage(null);
//            } else {
//
//                //If discord chat is enabled send the join message to discord also.
//                if (DISCORD_CHAT) {
//                    instance.getChat().broadcastDiscordAnnouncement(e.quitMessage(), "disconnect");
//                }
//            }
//
//        } else {
//            //Player is not leaving the network, so no disconnect message is appropriate.
//            e.quitMessage(null);
//        }
//
//        //If this is the last player on the server, remove all players from other servers from tab.
//        if (instance.getServer().getOnlinePlayers().size() == 1 && TAB) {
//            //Remove all fake players from tab since nobody is on this server.
//            for (String uuid : globalSQL.getStringList("SELECT uuid FROM online_users;")) {
//                instance.tab.removeFakePlayer(uuid);
//            }
//        }
    }

    /*
    A player has officially connected to the network if they have
    join the server but are not in the online_users table in the database.
     */
    //TODO: The follow code will be handled by the proxy.
//    public void networkJoinEvent(Player p) {
//
//        //If the user is not yet in the player_data table add them.
//        Component[] join_messages = new Component[1];
//
//        if (!globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + p.getUniqueId() + "';")) {
//
//            globalSQL.update("INSERT INTO player_data(uuid,name,last_online,last_submit,player_skin) VALUES('" +
//                    p.getUniqueId() + "','" + p.getName() + "'," + Time.currentTime() + "," + 0 + ",'" + TextureUtils.getTexture(p.getPlayerProfile()) + "');");
//
//            //Create the custom welcome messages if enabled.
//            if (CUSTOM_MESSAGES) {
//                join_messages[0] = LegacyComponentSerializer.legacyAmpersand().deserialize(firstJoinMessage.replace("%player%", p.getName()));
//            }
//        } else {
//
//            //Update the online time, name and player skin.
//            globalSQL.update("UPDATE player_data SET name='" + p.getName() + "',last_online=" + Time.currentTime() + ",player_skin='" + TextureUtils.getTexture(p.getPlayerProfile()) + "' WHERE uuid='" + p.getUniqueId() + "';");
//
//            //Create the custom join messages if enabled.
//            if (CUSTOM_MESSAGES) {
//                join_messages[0] = LegacyComponentSerializer.legacyAmpersand().deserialize(joinMessage.replace("%player%", p.getName()));
//            }
//        }
//
//        //Send global connect message.
//        //Add a slight delay so message can be seen by player joining.
//        Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {
//            if (CUSTOM_MESSAGES) {
//
//                instance.getChat().broadcastMessage(join_messages[0], "uknet:connect");
//
//                if (DISCORD_CHAT) {
//                    Component message = Component.text(TextureUtils.getAvatarUrl(p.getPlayerProfile()) + " ").append(join_messages[0]);
//                    instance.chat.broadcastDiscordAnnouncement(message, "connect");
//                }
//            }
//        }, 20L);
//
//        //Add user to table.
//        globalSQL.update("INSERT INTO online_users(uuid,join_time,last_ping,server,primary_role,display_name) VALUES('" + p.getUniqueId() +
//                "'," + Time.currentTime() + "," + Time.currentTime() + ",'" + SERVER_NAME + "','" + Roles.getPrimaryRole(p) + "','" +
//                PlaceholderAPI.setPlaceholders(p, "%luckperms_prefix%") + " " + p.getName() + "');");
//
//        if (p.hasPermission("group.reviewer")) {
//            //Show the number of submitted plots.
//            int plots = instance.getPlotSQL().getInt("SELECT COUNT(id) FROM plot_data WHERE status='submitted';");
//
//            if (plots != 0) {
//                if (plots == 1) {
//                    p.sendMessage(Utils.success("There is ")
//                            .append(Component.text(1, NamedTextColor.DARK_AQUA))
//                            .append(Utils.success(" plot available for review.")));
//                } else {
//                    p.sendMessage(Utils.success("There are ")
//                            .append(Component.text(plots, NamedTextColor.DARK_AQUA))
//                            .append(Utils.success(" plots available for review.")));
//                }
//            }
//
//            //Show the number of submitted regions requests.
//            int regions = instance.regionSQL.getInt("SELECT COUNT(region) FROM region_requests WHERE staff_accept=0;");
//
//            if (regions != 0) {
//                if (regions == 1) {
//                    p.sendMessage(Utils.success("There is ")
//                            .append(Component.text(1, NamedTextColor.DARK_AQUA))
//                            .append(Utils.success(" region request to review.")));
//                } else {
//                    p.sendMessage(Utils.success("There are ")
//                            .append(Component.text(regions, NamedTextColor.DARK_AQUA))
//                            .append(Utils.success(" region requests to review.")));
//                }
//            }
//
//            //Show the number of submitted navigation requests;
//            int navigation = globalSQL.getInt("SELECT COUNT(location) FROM location_requests;");
//
//            if (navigation != 0) {
//                if (navigation == 1) {
//                    p.sendMessage(Utils.success("There is ")
//                            .append(Component.text(1, NamedTextColor.DARK_AQUA))
//                            .append(Utils.success(" navigation request to review.")));
//                } else {
//                    p.sendMessage(Utils.success("There are ")
//                            .append(Component.text(navigation, NamedTextColor.DARK_AQUA))
//                            .append(Utils.success(" navigation requests to review.")));
//                }
//            }
//        }
//
//        //Log playercount in database
//        globalSQL.update("INSERT INTO player_count(log_time,players) VALUES(" + Time.currentTime() + "," +
//                globalSQL.getInt("SELECT count(uuid) FROM online_users;") + ");");
//
//    }

    private void serverSwitchEvent(Player p) {

        //Update server.
        globalSQL.update("UPDATE online_users SET server='" + SERVER_NAME + "' WHERE uuid='" + p.getUniqueId() + "';");

        //Remove their server_switch entry. Delayed by 1 second to make sure the previous server has run their PlayerQuitEvent.
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> globalSQL.update("DELETE FROM server_switch WHERE uuid='" + p.getUniqueId() + "';"), 20L);

        //Update the last_ping.
        globalSQL.update("UPDATE online_users SET last_ping=" + Time.currentTime() + " WHERE uuid='" + p.getUniqueId() + "' AND server='" + SERVER_NAME + "';");

    }

    /*
    A player has officially disconnected from the network after two
    unsuccessful pings by any network-connected server.
    A ping will occur on a one-second interval.
     */
    //TODO: The following code will be handled by the proxy.
//    public void networkLeaveEvent(OfflinePlayer p) {
//
//        String uuid = p.getUniqueId().toString();
//
//        //Remove any outstanding invites that this player has sent.
//        plotSQL.update("DELETE FROM plot_invites WHERE owner='" + uuid + "';");
//        plotSQL.update("DELETE FROM zone_invites WHERE owner='" + uuid + "';");
//        regionSQL.update("DELETE FROM region_invites WHERE owner='" + uuid + "';");
//
//        //Remove any outstanding invites that this player has received.
//        plotSQL.update("DELETE FROM plot_invites WHERE uuid='" + uuid + "';");
//        plotSQL.update("DELETE FROM zone_invites WHERE uuid='" + uuid + "';");
//        regionSQL.update("DELETE FROM region_invites WHERE uuid='" + uuid + "';");
//
//        //Set last_online time in playerdata.
//        globalSQL.update("UPDATE player_data SET last_online=" + Time.currentTime() + " WHERE UUID='" + uuid + "';");
//
//        //Get the player name and send global disconnect message.
//        String name = globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
//        String player_skin = globalSQL.getString("SELECT player_skin FROM player_data WHERE uuid='" + uuid + "';");
//
//        //Run disconnect message.
//        if (CUSTOM_MESSAGES) {
//
//            Component leave_message = LegacyComponentSerializer.legacyAmpersand().deserialize(leaveMessage.replace("%player%", name));
//            instance.chat.broadcastMessage(leave_message, "uknet:disconnect");
//
//            if (DISCORD_CHAT) {
//                Component message = Component.text(TextureUtils.getAvatarUrl(p.getUniqueId(), player_skin) + " ").append(leave_message);
//                instance.chat.broadcastDiscordAnnouncement(message, "disconnect");
//            }
//        }
//
//        //Remove player from online_users.
//        globalSQL.update("DELETE FROM online_users WHERE uuid='" + uuid + "';");
//
//        if (TAB) {
//            //Update tab for all players.
//            //This is done with the tab chat channel.
//            instance.chat.broadcastMessage(Component.text("remove " + p.getUniqueId()), "uknet:tab");
//        }
//
//        //Log playercount in database
//        globalSQL.update("INSERT INTO player_count(log_time,players) VALUES(" + Time.currentTime() + "," +
//                globalSQL.getInt("SELECT count(uuid) FROM online_users;") + ");");
//
//    }

}
