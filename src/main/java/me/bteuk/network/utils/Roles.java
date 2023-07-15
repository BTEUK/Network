package me.bteuk.network.utils;

import me.bteuk.network.Network;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import static me.bteuk.network.utils.Constants.DISCORD_LINKING;

public final class Roles {

    /*

        Get the builder role of the player.

        Builder roles include:
            Default (Guest)
            Applicant
            Apprentice
            Jr.Builder
            Builder
            Architect

     */

    public static String builderRole(Player p) {
        if (p.hasPermission("group.reviewer")) {
            return "reviewer";
        } else if (p.hasPermission("group.architect")) {
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
            return "default";
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
            return "Default";
        }
    }

    public static char tabSorting(String role) {
        return switch (role) {
            case "Admin" -> 'a';
            case "Mod" -> 'b';
            case "Support" -> 'c';
            case "Dev" -> 'd';
            case "Events" -> 'e';
            case "PR" -> 'f';
            case "Reviewer" -> 'g';
            case "Architect" -> 'h';
            case "Builder" -> 'i';
            case "Jr.Builder" -> 'j';
            case "Apprentice" -> 'k';
            case "Applicant" -> 'l';
            default -> 'm';
        };
    }

    public static void promoteBuilder(String uuid, String pRole, String nRole) {

        //Get console sender.
        ConsoleCommandSender console = Network.getInstance().getServer().getConsoleSender();

        //Remove current builder role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent remove " + pRole);

        //Add new builder role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent add " + nRole);

        //Update database.
        Network.getInstance().globalSQL.update("UPDATE player_data SET builder_role='" + nRole + "' WHERE uuid='" + uuid + "';");

        //Sync with discord if linked and enabled.
        if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM discord WHERE uuid='" + uuid + "';") && DISCORD_LINKING) {
            //Get discord id.
            long discord_id = Network.getInstance().globalSQL.getLong("SELECT discord_id FROM discord WHERE uuid='" + uuid + "';");

            //Sync roles.
            Network.getInstance().getTimers().discordSync(discord_id, nRole);

        }
    }
}
