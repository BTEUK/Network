package me.bteuk.network.lobby;

public abstract class AbstractReloadableComponent {

    private boolean enabled = false;

    /**
     * Unloads the component if enabled, and then loads it.
     */
    public final void reload() {
        if (enabled) {
            unload();
        }

        load();
    }

    /**
     * Load method.
     */
    public abstract void load();

    /**
     * Unload method.
     */
    public abstract void unload();

}
