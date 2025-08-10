package net.bteuk.network.eventing.events;

import net.bteuk.network.Network;
import net.bteuk.network.api.EventAPI;
import net.bteuk.network.api.entity.NetworkLocation;
import net.bteuk.network.commands.navigation.Back;
import net.bteuk.network.sql.GlobalSQL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import static net.bteuk.network.utils.Constants.SERVER_NAME;

public class EventManager extends AbstractEvent implements EventAPI {

    private final GlobalSQL globalSQL;

    public EventManager(GlobalSQL globalSQL) {
        this.globalSQL = globalSQL;
    }

    public void createJoinEvent(String uuid, String type, String event) {
        globalSQL.update(
                "INSERT INTO join_events(uuid,type,event) " + "VALUES('" + uuid + "','" + type + "','" + event + "') " + "ON DUPLICATE KEY UPDATE type='" + type + "', event='" + event + "';");
    }

    public void createJoinEvent(String uuid, String type, String event, String message) {
        Network.getInstance().getGlobalSQL()
                .update("INSERT INTO join_events(uuid,type,event,message) " + "VALUES('" + uuid + "','" + type + "','" + event + "','" + message + "') " + "ON DUPLICATE KEY " +
                        "UPDATE type='" + type + "', event='" + event + "', message='" + message + "';");
    }

    public void createJoinEvent(String uuid, String type, String event, Component message) {
        String messageString = PlainTextComponentSerializer.plainText().serialize(message);
        Network.getInstance().getGlobalSQL()
                .update("INSERT INTO join_events(uuid,type,event,message) " + "VALUES('" + uuid + "','" + type + "','" + event + "','" + message + "') " + "ON DUPLICATE KEY " +
                        "UPDATE type='" + type + "', event='" + event + "', message='" + messageString + "';");
    }

    /**
     * Creates an event with the following input parameters:
     *
     * @param uuid   the uuid of the player to which the event should apply
     * @param type   the type of event, this means the plugin which should run this event (network, plotsystem or a
     *               custom implementation)
     * @param server the server name where the event should occur
     * @param event  the event arguments in String format
     */
    public void createEvent(String uuid, String type, String server, String event) {
        if (uuid == null) {
            Network.getInstance().getGlobalSQL().update("INSERT INTO server_events(type,server,event) " + "VALUES('" + type + "','" + server + "','" + event + "');");
        } else {
            Network.getInstance().getGlobalSQL()
                    .update("INSERT INTO server_events(uuid,type,server,event) " + "VALUES('" + uuid + "','" + type + "','" + server + "','" + event + "');");
        }
    }

    /**
     * Creates an event with the following input parameters:
     *
     * @param uuid    the uuid of the player to which the event should apply
     * @param type    the type of event, this means the plugin which should run this event (network, plotsystem or a
     *                custom implementation)
     * @param server  the server name where the event should occur
     * @param event   the event arguments in String format
     * @param message message to be sent to the player on success
     */
    public void createEvent(String uuid, String type, String server, String event, String message) {
        if (uuid == null) {
            Network.getInstance().getGlobalSQL()
                    .update("INSERT INTO server_events(type,server,event,message) " + "VALUES('" + type + "','" + server + "','" + event + "','" + message + "');");
        } else {
            Network.getInstance().getGlobalSQL()
                    .update("INSERT INTO server_events(uuid,type,server,event,message) " + "VALUES('" + uuid + "','" + type + "','" + server + "','" + event + "','" + message +
                            "');");
        }
    }

    /**
     * Creates an event with the following input parameters:
     *
     * @param uuid    the uuid of the player to which the event should apply
     * @param type    the type of event, this means the plugin which should run this event (network, plotsystem or a
     *                custom implementation)
     * @param server  the server name where the event should occur
     * @param event   the event arguments in String format
     * @param message message to be sent to the player on success
     */
    public void createEvent(String uuid, String type, String server, String event, Component message) {
        String messageString = PlainTextComponentSerializer.plainText().serialize(message);
        if (uuid == null) {
            Network.getInstance().getGlobalSQL()
                    .update("INSERT INTO server_events(type,server,event,message) " + "VALUES('" + type + "','" + server + "','" + event + "','" + messageString + "');");
        } else {
            Network.getInstance().getGlobalSQL()
                    .update("INSERT INTO server_events(uuid,type,server,event,message) " + "VALUES('" + uuid + "','" + type + "','" + server + "','" + event + "','" + messageString + "');");
        }
    }

    public void createTeleportEvent(boolean join, String uuid, String type, String event, NetworkLocation previousLocation) {

        Back.setPreviousCoordinate(uuid, previousLocation);

        // Create event
        if (join) {
            createJoinEvent(uuid, type, event);
        } else {
            createEvent(uuid, type, SERVER_NAME, event);
        }
    }

    public void createTeleportEvent(boolean join, String uuid, String type, String event, String message, NetworkLocation previousLocation) {

        Back.setPreviousCoordinate(uuid, previousLocation);

        // Create event
        if (join) {
            createJoinEvent(uuid, type, event, message);
        } else {
            createEvent(uuid, type, SERVER_NAME, event, message);
        }
    }

    public void createTeleportEvent(boolean join, String uuid, String type, String event, Component message, NetworkLocation previousLocation) {

        String messageString = PlainTextComponentSerializer.plainText().serialize(message);
        Back.setPreviousCoordinate(uuid, previousLocation);

        // Create event
        if (join) {
            createJoinEvent(uuid, type, event, messageString);
        } else {
            createEvent(uuid, type, SERVER_NAME, event, messageString);
        }
    }

    /**
     * General implementation of an event, uses a switch expression to run the actual event.
     *
     * @param uuid    the uuid of the player to whom this event applies
     * @param event   arguments of the event
     * @param message optional message to send to the player after the event has executes successfully
     */
    @Override
    public void event(String uuid, String[] event, String message) {

        // Start the execution process by looking at the event message structure.
        switch (event[0]) {
            case "invite" -> new InviteEvent().event(uuid, event, message);
            case "teleport" -> new TeleportEvent().event(uuid, event, message);
            case "region" -> new RegionEvent().event(uuid, event, message);
            case "kick" -> new KickEvent().event(uuid, event, message);
        }
    }
}
