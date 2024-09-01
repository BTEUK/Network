package net.bteuk.network.eventing.events;

/**
 * Abstract class for events.
 */
public abstract class AbstractEvent {

    /**
     * abstract event method, all events must use this structure.
     *
     * @param uuid
     * the uuid of the player to whom this event applies
     * @param args
     * arguments of the event
     * @param message
     * optional message to send to the player after the event has executes successfully
     */
    public abstract void event(String uuid, String[] args, String message);

}
