package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import org.bukkit.Bukkit;

import java.util.UUID;

public class InviteEvent {

    public static void event(String uuid, String[] event) {

        if (event[1].equals("plot")) {

            //Get player.
            Bukkit.getPlayer(UUID.fromString(uuid));

            //Invite user to plot.



        }

    }
}
