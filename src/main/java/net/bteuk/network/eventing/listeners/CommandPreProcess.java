package net.bteuk.network.eventing.listeners;

import net.bteuk.network.Network;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static net.bteuk.network.commands.AFK.updateAfkStatus;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.Constants.REGIONS_ENABLED;
import static net.bteuk.network.utils.Constants.SERVER_NAME;
import static net.bteuk.network.utils.Constants.TPLL_ENABLED;

public class CommandPreProcess implements Listener {

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
            NetworkUser user = instance.getUser(e.getPlayer());

            //If u is null, cancel.
            if (user == null) {
                LOGGER.severe("User " + e.getPlayer().getName() + " can not be found!");
                e.getPlayer().sendMessage(ChatUtils.error("User can not be found, please relog!"));
                e.setCancelled(true);
                return;
            }

            user.last_movement = Time.currentTime();
            if (user.afk) {
                user.afk = false;
                updateAfkStatus(user, false);
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
        } else if (isCommand(e.getMessage(), "/me")) {
            // This command is not allowed.
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatUtils.error("You do not have permission to use this command."));
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
        instance.getGlobalSQL().update("UPDATE server_data SET online=0 WHERE name='" + SERVER_NAME + "';");

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
        if (instance.getGlobalSQL().hasRow("SELECT name FROM server_data WHERE type='LOBBY' AND online=1;")) {

            server = instance.getGlobalSQL().getString("SELECT name FROM server_data WHERE type='LOBBY' AND online=1;");

        } else if (instance.getGlobalSQL().hasRow("SELECT name FROM server_data WHERE type='EARTH' AND online=1;")) {

            server = instance.getGlobalSQL().getString("SELECT name FROM server_data WHERE type='EARTH' AND online=1;");

        } else if (instance.getGlobalSQL().hasRow("SELECT name FROM server_data WHERE online=1;")) {

            server = instance.getGlobalSQL().getString("SELECT name FROM server_data WHERE online=1;");

        }

        for (NetworkUser user : users) {

            user.last_movement = Time.currentTime();
            if (server != null) {

                if (user.afk) {
                    user.afk = false;
                    updateAfkStatus(user, false);
                }


                //Switch the player to that server.
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(stream);
                    out.writeUTF("Connect");
                    out.writeUTF(server);
                    user.player.sendPluginMessage(instance, "BungeeCord", stream.toByteArray());
                } catch (IOException e) {
                    LOGGER.severe("IOException when attempting to switch player to another server.");
                    return;
                }

            } else {

                if (user.afk) {
                    user.afk = false;
                }

                String uuid = user.player.getUniqueId().toString();

                //Remove any outstanding invites that this player has sent.
                instance.getPlotSQL().update("DELETE FROM plot_invites WHERE owner='" + uuid + "';");

                //Remove any outstanding invites that this player has received.
                instance.getPlotSQL().update("DELETE FROM plot_invites WHERE uuid='" + uuid + "';");

                //Set last_online time in playerdata.
                instance.getGlobalSQL().update("UPDATE player_data SET last_online=" + Time.currentTime() + " WHERE UUID='" + uuid + "';");

                //Remove player from online_users.
                //Since this closes the server tab does not need to be updated for these players.
                instance.getGlobalSQL().update("DELETE FROM online_users WHERE uuid='" + uuid + "';");

                //Kick the player.
                user.player.kick(Component.text("The server is restarting!", NamedTextColor.RED));

                //Send the disconnect message in discord, since the standard leaveserver event has been blocked.
                String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
                String player_skin = Network.getInstance().getGlobalSQL().getString("SELECT player_skin FROM player_data WHERE uuid='" + uuid + "';");

//                //Run disconnect message.
                //TODO
//                if (DISCORD_CHAT) {
//                    Component message = Component.text(TextureUtils.getAvatarUrl(u.player.getUniqueId(), player_skin) + " ")
//                            .append(LegacyComponentSerializer.legacyAmpersand().deserialize(Objects.requireNonNull(CONFIG.getString("chat.custom_messages.leave")).replace("%player%", name)));
//                    instance.getChat().broadcastDiscordAnnouncement(message, "disconnect");
//                }
            }
        }

        //Block movement and teleport listeners.
        instance.moveListener.block();
        instance.teleportListener.block();

        //Remove users from list.
        users.clear();

    }
}
