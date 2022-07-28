package me.bteuk.network.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import org.bukkit.entity.Player;

public class SwitchServer {

    public static void switchServer(Player p, String server) {

        //Add switch server instance in database.
        Network.getInstance().globalSQL.update("INSERT INTO server_switch(uuid,from_server,to_server,switch_time) VALUES('" +
                p.getUniqueId() + "','" + Network.SERVER_NAME + "','" + server + "'," + Time.currentTime() + ");");

        //Switch Server
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("building");
        p.sendPluginMessage(Network.getInstance(), "BungeeCord", out.toByteArray());

    }
}
