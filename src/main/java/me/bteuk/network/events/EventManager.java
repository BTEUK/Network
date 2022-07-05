package me.bteuk.network.events;

public class EventManager {

    public static void event(String uuid, String[] event) {

        //Start the execution process by looking at the event message structure.
        if (event[0].equals("invite")) {

            InviteEvent.event(uuid, event);

        } else if (event[0].equals("teleport")) {

            TeleportEvent.event(uuid, event);

        }
    }
}
