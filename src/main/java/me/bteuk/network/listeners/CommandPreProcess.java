package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.exceptions.NotMutedException;
import me.bteuk.network.utils.*;
import me.bteuk.network.utils.staff.Moderation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static me.bteuk.network.utils.Constants.*;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public class CommandPreProcess extends Moderation implements Listener {

    private final Network instance;

    public CommandPreProcess(Network instance) {
        this.instance = instance;
        instance.allow_shutdown = false;
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {

        //Reset afk status.
        if (!e.getMessage().startsWith("/afk")) {

            //If player is afk, unset it.
            //Reset last logged time.
            NetworkUser u = instance.getUser(e.getPlayer());

            //If u is null, cancel.
            if (u == null) {
                LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
                e.getPlayer().sendMessage(Utils.error("User can not be found, please relog!"));
                e.setCancelled(true);
                return;
            }

            u.last_movement = Time.currentTime();
            if (u.afk) {
                u.last_time_log = Time.currentTime();
                u.afk = false;
                Network.getInstance().chat.broadcastAFK(u.player, false);
            }

        }

        //Replace /region with /network:region
        if (isCommand(e.getMessage(), "/region")) {
            if (REGIONS_ENABLED) {
                e.setMessage(e.getMessage().replace("/region", "/network:region"));
            }

        } else if (isCommand(e.getMessage(), "/tpll")) {
            if (TPLL_ENABLED) {
                e.setMessage(e.getMessage().replace("/tpll", "/network:tpll"));
            }

        } else if (isCommand(e.getMessage(), "/server")) {
            e.setMessage(e.getMessage().replace("/server", "/network:server"));

        } else if (isCommand(e.getMessage(), "/hdb")) {
            //If skulls plugin exists and is loaded.
            if (Bukkit.getServer().getPluginManager().getPlugin("skulls") != null) {
                e.setMessage(e.getMessage().replace("/hdb", "/skulls"));
            }
        } else if (isCommand(e.getMessage(), "/tell", "/msg", "/w", "/me")) {
            //If player is muted cancel.
            if (isMuted(e.getPlayer().getUniqueId().toString())) {
                e.setCancelled(true);
                try {

                    //Send message and end event.
                    e.getPlayer().sendMessage(getMutedComponent(e.getPlayer().getUniqueId().toString()));

                } catch (NotMutedException ex) {

                    //Unset the muted status.
                    e.setCancelled(false);

                }
            }
        }
    }

    /**
     * Checks whether a command sent is equal to another command.
     *
     * @param message Command message by the sender
     * @param command Command to check for
     * @return true is equals, false otherwise
     */
    private boolean isCommand(String message, String command) {
        return (message.startsWith(command + " ") || message.equalsIgnoreCase(command));
    }

    private boolean isCommand(String message, String... commands) {
        for (String command : commands) {
            if (isCommand(message, command)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void serverCommand(ServerCommandEvent s) {
        if (s.getCommand().equalsIgnoreCase("stop")) {
            if (!instance.allow_shutdown) {
                instance.allow_shutdown = true;
                onServerClose(instance.getUsers());

                //Delay shutdown by 3 seconds to make sure players have switched server.
                s.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop"), 60L);
            }
        }
    }

    //This class executes when the server closes, instead of a player server quit event since that will cause errors.
    //It for the most part copies the methods.
    public void onServerClose(ArrayList<NetworkUser> users) {

        //Disable server in server table.
        instance.globalSQL.update("UPDATE server_data SET online=0 WHERE name='" + SERVER_NAME + "';");

        //Block the LeaveServer listener so it doesn't trigger since it causes an error.
        //It needs to be active to prevent the leave message to show in chat.
        if (instance.getConnect() != null) {
            instance.getConnect().setBlockLeaveEvent(true);
        }

        //Check if another server is online,
        //If true then switch all the players to this server.
        //Always check the lobby and earth first.
        //Remove all players from network.
        String server = null;

        //Try different servers.
        if (instance.globalSQL.hasRow("SELECT name FROM server_data WHERE type='LOBBY' AND online=1;")) {

            server = instance.globalSQL.getString("SELECT name FROM server_data WHERE type='LOBBY' AND online=1;");

        } else if (instance.globalSQL.hasRow("SELECT name FROM server_data WHERE type='EARTH' AND online=1;")) {

            server = instance.globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH' AND online=1;");

        } else if (instance.globalSQL.hasRow("SELECT name FROM server_data WHERE online=1;")) {

            server = instance.globalSQL.getString("SELECT name FROM server_data WHERE online=1;");

        }

        for (NetworkUser u : users) {

            u.last_movement = Time.currentTime();
            if (server != null) {

                //Reset last logged time.
                if (u.afk) {
                    u.last_time_log = Time.currentTime();
                    u.afk = false;
                    Network.getInstance().chat.broadcastAFK(u.player, false);
                }


                //Switch the player to that server.
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(stream);
                    out.writeUTF("Connect");
                    out.writeUTF(server);
                    u.player.sendPluginMessage(instance, "BungeeCord", stream.toByteArray());
                } catch (IOException e) {
                    LOGGER.severe("IOException when attempting to switch player to another server.");
                    return;
                }

            } else {

                //Reset last logged time.
                if (u.afk) {
                    u.last_time_log = Time.currentTime();
                    u.afk = false;
                }

                String uuid = u.player.getUniqueId().toString();

                //Remove any outstanding invites that this player has sent.
                instance.getPlotSQL().update("DELETE FROM plot_invites WHERE owner='" + uuid + "';");

                //Remove any outstanding invites that this player has received.
                instance.getPlotSQL().update("DELETE FROM plot_invites WHERE uuid='" + uuid + "';");

                //Set last_online time in playerdata.
                instance.globalSQL.update("UPDATE player_data SET last_online=" + Time.currentTime() + " WHERE UUID='" + uuid + "';");

                //Remove player from online_users.
                //Since this closes the server tab does not need to be updated for these players.
                instance.globalSQL.update("DELETE FROM online_users WHERE uuid='" + uuid + "';");

                //Log playercount in database
                instance.globalSQL.update("INSERT INTO player_count(log_time,players) VALUES(" + Time.currentTime() + "," +
                        instance.globalSQL.getInt("SELECT count(uuid) FROM online_users;") + ");");

                //Kick the player.
                u.player.kick(Component.text("The server is restarting!", NamedTextColor.RED));

                //Send the disconnect messagein discord, since the standard leaveserver event has been blocked.
                String name = Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
                String player_skin = Network.getInstance().globalSQL.getString("SELECT player_skin FROM player_data WHERE uuid='" + uuid + "';");

                //Run disconnect message.
                if (DISCORD_CHAT) {
                    Component message = Component.text(TextureUtils.getAvatarUrl(u.player.getUniqueId(), player_skin) + " ")
                            .append(LegacyComponentSerializer.legacyAmpersand().deserialize(Objects.requireNonNull(CONFIG.getString("chat.custom_messages.leave")).replace("%player%", name)));
                    instance.chat.broadcastDiscordAnnouncement(message, "disconnect");
                }

            }

            //Update statistics
            long time = Time.currentTime();
            Statistics.save(u, Time.getDate(time), time);

        }

        //Block movement and teleport listeners.
        instance.moveListener.block();
        instance.teleportListener.block();

        //Remove users from list.
        users.clear();

    }
}
