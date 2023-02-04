package me.bteuk.network.utils;

import me.bteuk.network.Network;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
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
        return PlaceholderAPI.setPlaceholders(p, "%luckperms_current_group_on_track_builder%");
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

    public static void promoteBuilder(String uuid, String pRole, String nRole) {

        //Get console sender.
        ConsoleCommandSender console = Network.getInstance().getServer().getConsoleSender();

        //Remove current builder role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent remove " + pRole);

        //Add new builder role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent add " + nRole);

        //Update database.
        Network.getInstance().globalSQL.update("UPDATE player_data SET builder_role='" + nRole + "' WHERE uuid='" + uuid + "';");

        //Sync with discord if linked.
        if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM discord WHERE uuid='" + uuid + "';")) {
            //Get discord id.
            long discord_id = Network.getInstance().globalSQL.getLong("SELECT discord_id FROM discord WHERE uuid='" + uuid + "';");

            //Sync roles.
            Network.getInstance().timers.discordSync(discord_id, nRole);

        }
    }
}
