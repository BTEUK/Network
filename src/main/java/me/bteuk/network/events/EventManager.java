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
}
