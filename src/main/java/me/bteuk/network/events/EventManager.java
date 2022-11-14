package me.bteuk.network.events;

import me.bteuk.network.Network;
import me.bteuk.network.commands.Back;
import org.bukkit.Location;

public class EventManager {

    public static void event(String uuid, String[] event, String message) {

        //Start the execution process by looking at the event message structure.
        switch (event[0]) {
            case "invite" -> InviteEvent.event(uuid, event);
            case "teleport" -> TeleportEvent.event(uuid, event, message);
            case "region" -> RegionEvent.event(uuid, event);
        }
    }

    public static void createJoinEvent(String uuid, String type, String event) {
        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) " +
                "VALUES('" + uuid + "','" + type + "','" + event + "');");
    }

    public static void createJoinEvent(String uuid, String type, String event, String message) {
        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event,message) " +
                "VALUES('" + uuid + "','" + type + "','" + event + "','" + message + "');");
    }

    public static void createEvent(String uuid, String type, String server, String event) {
        if (uuid == null) {
            Network.getInstance().globalSQL.update("INSERT INTO server_events(type,server,event) " +
                    "VALUES('" + type + "','" + server + "','" + event + "');");
        } else {
            Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) " +
                    "VALUES('" + uuid + "','" + type + "','" + server + "','" + event + "');");
        }
    }

    public static void createEvent(String uuid, String type, String server, String event, String message) {
        if (uuid == null) {
            Network.getInstance().globalSQL.update("INSERT INTO server_events(type,server,event,message) " +
                    "VALUES('" + type + "','" + server + "','" + event + "','" + message + "');");
        } else {
            Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event,message) " +
                    "VALUES('" + uuid + "','" + type + "','" + server + "','" + event + "','" + message + "');");
        }
    }

    public static void createTeleportEvent(boolean join, String uuid, String type, String event, Location previousLocation) {

        Back.setPreviousCoordinate(uuid, previousLocation);

        //Create event
        if (join) {
            createJoinEvent(uuid, type, event);
        } else {
            createEvent(uuid, type, Network.SERVER_NAME, event);
        }
    }

    public static void createTeleportEvent(boolean join, String uuid, String type, String event, String message, Location previousLocation) {

        Back.setPreviousCoordinate(uuid, previousLocation);

        //Create event
        if (join) {
            createJoinEvent(uuid, type, event, message);
        } else {
            createEvent(uuid, type, Network.SERVER_NAME, event, message);
        }
    }
}
