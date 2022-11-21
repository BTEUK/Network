package me.bteuk.network.staff;

/*

This class will have all the functionality dealing with moderation.
This includes /ban /mute /kick

 */

import me.bteuk.network.Network;
import me.bteuk.network.utils.Time;

public class Moderation {

    //Ban the player.
    public void ban(String uuid, long end_time, String reason) {

        //Get time.
        long time = Time.currentTime();

        //If the player is already banned, end the old ban.
        if (isBanned(uuid)) {
            Network.getInstance().globalSQL.update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='ban';");
        }
        Network.getInstance().globalSQL.update("INSERT INTO moderation(uuid,start_time,end_time,reason,type) VALUES('" + uuid + "'," + time + "," + end_time + ",'" + reason + "','ban');");
    }

    //Mute the player.
    public void mute(String uuid, long end_time, String reason) {

        //Get time.
        long time = Time.currentTime();

        //If the player is already banned, end the old ban.
        if (isBanned(uuid)) {
            Network.getInstance().globalSQL.update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='mute';");
        }
        Network.getInstance().globalSQL.update("INSERT INTO moderation(uuid,start_time,end_time,reason,type) VALUES('" + uuid + "'," + time + "," + end_time + ",'" + reason + "','mute');");
    }

    //Unban the player.
    public void unban(String uuid) {
        //Get time.
        long time = Time.currentTime();
        Network.getInstance().globalSQL.update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='ban';");
    }

    //Unmute the player.
    public void unmute(String uuid) {
        //Get time.
        long time = Time.currentTime();
        Network.getInstance().globalSQL.update("UPDATE moderation SET end_time=" + time + " WHERE uuid='" + uuid + "' AND end_time>" + time + " AND type='mute';");
    }

    //If the player is currently banned, return true.
    public boolean isBanned(String uuid) {
        return (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='ban';"));
    }

    //If the player is currently muted, return true.
    public boolean isMuted(String uuid) {
        return (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='mute';"));
    }

    //Get reason why player is banned.
    public String getBannedReason(String uuid) {
        return (Network.getInstance().globalSQL.getString("SELECT reason FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='ban';"));
    }

    //Get reason why player is muted.
    public String getMutedReason(String uuid) {
        return (Network.getInstance().globalSQL.getString("SELECT reason FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='mute';"));
    }

    //Get duration of ban.
    public String getBanDuration(String uuid) {
        long time = Network.getInstance().globalSQL.getLong("SELECT end_time FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='ban';");
        return Time.getDateTime(time);
    }

    //Get duration of mute.
    public String getMuteDuration(String uuid) {
        long time = Network.getInstance().globalSQL.getLong("SELECT end_time FROM moderation WHERE uuid='" + uuid + "' AND end_time>" + Time.currentTime() + " AND type='mute';");
        return Time.getDateTime(time);
    }
}
