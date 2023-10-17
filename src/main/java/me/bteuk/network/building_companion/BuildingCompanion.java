package me.bteuk.network.building_companion;

import me.bteuk.network.utils.NetworkUser;
import org.bukkit.entity.Player;

/**
 * This class stored all the information about the building companion.
 * It is player-specific and will be enabled when a player activates it.
 * Listeners will be registered in here, along with any tool-related variables.
 */
public class BuildingCompanion {

    //The player that this building companion is for.
    private final NetworkUser user;

    public BuildingCompanion(NetworkUser user) {

        this.user = user;

    }

    protected boolean playerEquals(Player p) {
        return p.equals(user.player);
    }
}
