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

    public static String getPrimaryRole(Player p) {
        if (p.hasPermission("group.administrator")) {
            return "Admin";
        } else if (p.hasPermission("group.moderator")) {
            return "Mod";
        } else if (p.hasPermission("group.support")) {
            return "Support";
        } else if (p.hasPermission("group.developer")) {
            return "Dev";
        } else if (p.hasPermission("group.eventsteam")) {
            return "Events";
        } else if (p.hasPermission("group.publicrelations")) {
            return "PR";
        } else if (p.hasPermission("group.reviewer")) {
            return "Reviewer";
        } else if (p.hasPermission("group.architect")) {
            return "Architect";
        } else if (p.hasPermission("group.builder")) {
            return "Builder";
        } else if (p.hasPermission("group.jrbuilder")) {
            return "Jr.Builder";
        } else if (p.hasPermission("group.apprentice")) {
            return "Apprentice";
        } else if (p.hasPermission("group.applicant")) {
            return "Applicant";
        } else {
            return "Guest";
        }
    }
}
