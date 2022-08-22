package me.bteuk.network.utils;

import org.bukkit.entity.Player;

public final class Roles {

    /*

        Get the builder role of the player.

        Builder roles include:
            Guest (Default)
            Applicant
            Apprentice
            Jr.Builder
            Builder
            Architect

     */

    public static String builderRole(Player p) {
        if (p.hasPermission("group.architect")) {
            return "architect";
        } else if (p.hasPermission("group.builder")) {
            return "builder";
        } else if (p.hasPermission("group.jrbuilder")) {
            return "jrbuilder";
        } else if (p.hasPermission("group.apprentice")) {
            return "apprentice";
        } else if (p.hasPermission("group.applicant")) {
            return "applicant";
        } else {
            return "guest";
        }
    }
}
