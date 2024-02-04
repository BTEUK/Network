package me.bteuk.network.lobby;

public abstract class AbstractLobbyComponent {

    private boolean enabled = false;

    /**
     * Standard reloading method for a {@link LobbyComponent}.
     * Unloads the component if enabled, and then loads it.
     */
    public final void reload() {
        if (enabled) {
            unload();
        }

        load();
    }

    /**
     * Load method for loading a {@link LobbyComponent}
     */
    public abstract void load();

    /**
     * Unload method for unloading a {@link LobbyComponent}
     */
    public abstract void unload();

}
