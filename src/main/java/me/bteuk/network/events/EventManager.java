package me.bteuk.network.events;

import me.bteuk.network.Network;

public class EventManager {

    public static void event(String uuid, String[] event) {

        //Start the execution process by looking at the event message structure.
        switch (event[0]) {
            case "invite" -> InviteEvent.event(uuid, event);
            case "teleport" -> TeleportEvent.event(uuid, event);
            case "region" -> RegionEvent.event(uuid, event);
        }
    }

    public static void createJoinEvent(String uuid, String type, String event) {
        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) " +
                "VALUES('" + uuid + "','" + type + "','" + event + "');");
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
}
