package me.bteuk.network.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.utils.TextureUtils;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import static me.bteuk.network.utils.NetworkConfig.CONFIG;

//This class deals with players joining and leaving the network.
public class Connect {

    private final Network instance;

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    private final String joinMessage;
    private final String firstJoinMessage;
    private final String leaveMessage;

    public Connect(Network instance, GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.instance = instance;

        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

        //Get join and leave message from config.
        joinMessage = CONFIG.getString("chat.join");
        firstJoinMessage = CONFIG.getString("chat.firstjoin");
        leaveMessage = CONFIG.getString("chat.leave");

    }

    /*
    A player has officially connected to the network if they have
    join the server but are not in the online_users table in the database.
     */
    public void joinEvent(Player p) {

        //If the user is not yet in the player_data table add them.
        if (!globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid='" + p.getUniqueId() + "';")) {

            globalSQL.update("INSERT INTO player_data(uuid,name,last_online,last_submit,player_skin) VALUES('" +
                    p.getUniqueId() + "','" + p.getName() + "'," + Time.currentTime() + "," + 0 + ",'" + TextureUtils.getTexture(p.getPlayerProfile()) + "');");

            //Send global welcome message.
            //Add a slight delay so message can be seen by player joining.
            Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {

                instance.chat.broadcastMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(firstJoinMessage.replace("%player%", p.getName())), "uknet:connect");

                instance.chat.broadcastMessage(Component.text(TextureUtils.getAvatarUrl(p.getPlayerProfile()) + " ")
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize(firstJoinMessage.replace("%player%", p.getName()))), "uknet:discord_connect");
            }, 20L);

        } else {

            //Update the online time, name and player skin.
            globalSQL.update("UPDATE player_data SET name='" + p.getName() + "',last_online=" + Time.currentTime() + ",player_skin='" + TextureUtils.getTexture(p.getPlayerProfile()) + "' WHERE uuid='" + p.getUniqueId() + "';");

            //Send global connect message.
            //Add a slight delay so message can be seen by player joining.
            Bukkit.getScheduler().runTaskLater(Network.getInstance(), () -> {

                instance.chat.broadcastMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(joinMessage.replace("%player%", p.getName())), "uknet:connect");

                instance.chat.broadcastMessage(Component.text(TextureUtils.getAvatarUrl(p.getPlayerProfile()) + " ")
                                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(joinMessage.replace("%player%", p.getName())))
                        , "uknet:discord_connect");
            }, 1L);

        }

        if (p.hasPermission("group.reviewer")) {
            int plots = instance.plotSQL.getInt("SELECT COUNT(id) FROM plot_data WHERE status='submitted';");

            if (plots != 0) {
                if (plots == 1) {
                    p.sendMessage(Utils.success("There is &31 &aplot available for review."));
                } else {
                    p.sendMessage(Utils.success("There are &3" + plots + " &aplots available for review."));
                }
            }
        }

        //Log playercount in database
        globalSQL.update("INSERT INTO player_count(log_time,players) VALUES(" + Time.currentTime() + "," +
                globalSQL.getInt("SELECT count(uuid) FROM online_users;") + ");");

    }

    /*
    A player has officially disconnected from the network after two
    unsuccessful pings by any network-connected server.
    A ping will occur on a one-second interval.
     */
    public void leaveEvent(OfflinePlayer p) {

        String uuid = p.getUniqueId().toString();

        //Remove any outstanding invites that this player has sent.
        plotSQL.update("DELETE FROM plot_invites WHERE owner='" + uuid + "';");
        plotSQL.update("DELETE FROM zone_invites WHERE owner='" + uuid + "';");

        //Remove any outstanding invites that this player has received.
        plotSQL.update("DELETE FROM plot_invites WHERE uuid='" + uuid + "';");
        plotSQL.update("DELETE FROM zone_invites WHERE uuid='" + uuid + "';");

        //Set last_online time in playerdata.
        globalSQL.update("UPDATE player_data SET last_online=" + Time.currentTime() + " WHERE UUID='" + uuid + "';");

        //Get the player name and send global disconnect message.
        String name = globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");
        String player_skin = globalSQL.getString("SELECT player_skin FROM player_data WHERE uuid='" + uuid + "';");

        //Run disconnect message.
        instance.chat.broadcastMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(leaveMessage.replace("%player%", name)), "uknet:disconnect");

        instance.chat.broadcastMessage(Component.text(TextureUtils.getAvatarUrl(name, p.getUniqueId(), player_skin) + " ")
                        .append(LegacyComponentSerializer.legacyAmpersand().deserialize(leaveMessage.replace("%player%", name)))
                , "uknet:discord_disconnect");

        //Remove player from online_users.
        globalSQL.update("DELETE FROM online_users WHERE uuid='" + uuid + "';");

        //Update tab for all players.
        //This is done with the tab chat channel.
        instance.chat.broadcastMessage(Component.text("remove " + p.getUniqueId()), "uknet:tab");

        //Log playercount in database
        globalSQL.update("INSERT INTO player_count(log_time,players) VALUES(" + Time.currentTime() + "," +
                globalSQL.getInt("SELECT count(uuid) FROM online_users;") + ");");

    }

}
