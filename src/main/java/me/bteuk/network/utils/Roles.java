package me.bteuk.network.utils;

import me.bteuk.network.Network;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import static me.bteuk.network.utils.Constants.DISCORD_CHAT;
import static me.bteuk.network.utils.Constants.DISCORD_LINKING;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

public final class Roles {

    private static final Component PROMOTION_TEMPLATE = Component.text(" has been promoted to ");
    private static final String PROMOTION_SELF = "You have been promoted to ";

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

        //Add new builder role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent add " + nRole);

        //Remove current builder role. Remove after adding to make sure the player always has a role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent remove " + pRole);

        //Update database.
        Network.getInstance().getGlobalSQL().update("UPDATE player_data SET builder_role='" + nRole + "' WHERE uuid='" + uuid + "';");

        //Sync with discord if linked and enabled.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM discord WHERE uuid='" + uuid + "';") && DISCORD_LINKING) {
            //Get discord id.
            long discord_id = Network.getInstance().getGlobalSQL().getLong("SELECT discord_id FROM discord WHERE uuid='" + uuid + "';");

            //Sync roles.
            Network.getInstance().getTimers().discordSync(discord_id, nRole);

        }

        //Announce the promotion in chat and discord.
        //Send a message to the user if not online, so they'll be notified of their promotion next time they join the server.
        String colouredRole = CONFIG.getString("roles." + nRole + ".colour") + CONFIG.getString("roles." + nRole + ".name");
        if (CONFIG.getBoolean("chat.announce_promotions")) {
            String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");


            Component promotation_message = Component.text(name)
                    .append(PROMOTION_TEMPLATE)
                    .append(LegacyComponentSerializer.legacyAmpersand().deserialize(colouredRole));

            Network.getInstance().chat.broadcastMessage(promotation_message, "uknet:globalchat");
            //Add discord formatting to make the message bold.
            if (DISCORD_CHAT) {
                Network.getInstance().chat.broadcastDiscordAnnouncement(promotation_message, "promotion");
            }
        }

        //Check if the player is online.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {

            //Send a message that will show when they next log in.
            Network.getInstance().getGlobalSQL().update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" +
                    PROMOTION_SELF + colouredRole
                    + "');");

        }
    }

    /**
     * Maps a technical role name to a display role name.
     *
     * @param role the role to map
     * @return the display name of that role
     */
    public static String roleMapping(String role) {

        switch (role) {

            case "applicant" -> {
                return ("Applicant");
            }

            case "apprentice" -> {
                return ("Apprentice");
            }

            case "jrbuilder" -> {
                return ("Jr.Builder");
            }

            case "builder" -> {
                return ("Builder");
            }

            case "architect" -> {
                return ("Architect");
            }

            case "reviewer" -> {
                return ("Reviewer");
            }

            default -> {
                return ("Default");
            }
        }
    }
}
