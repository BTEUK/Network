package net.bteuk.network.utils;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.ChatMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import static net.bteuk.network.lib.enums.ChatChannels.GLOBAL;
import static net.bteuk.network.utils.Constants.DISCORD_LINKING;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public final class Roles {

    private static final Component PROMOTION_TEMPLATE = Component.text(" has been promoted to ");
    private static final String PROMOTION_SELF = "You have been promoted to ";

    private static Set<Role> ROLES;

    public static Set<Role> getRoles() {
        if (ROLES == null) {
            loadRoles();
        }
        return ROLES;
    }

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

    public static Role getPrimaryRole(Player p) {
        // Get the configuration if not yet fetches.
        if (ROLES == null) {
            loadRoles();
        }
        for (Role role : ROLES) {
            if (p.hasPermission(String.format("group.%s", role.getId()))) {
                return role;
            }
        }
        return null;
    }

    private static void loadRoles() {
        // Create roles.yml if not exists.
        // The data folder should already exist since the plugin will always create config.yml first.
        File rolesFile = new File(Network.getInstance().getDataFolder(), "roles.yml");
        if (!rolesFile.exists()) {
            Network.getInstance().saveResource("roles.yml", false);
        }

        FileConfiguration rolesConfig = YamlConfiguration.loadConfiguration(rolesFile);

        // Gets all the roles from the config.
        ConfigurationSection roles = rolesConfig.getConfigurationSection("roles");

        if (roles == null) {
            return;
        }

        Set<String> keys = roles.getKeys(false);

        ROLES = new TreeSet<>();
        // Add the roles.
        keys.forEach(key -> ROLES.add(new Role(
                key,
                roles.getString(key + "name", null),
                roles.getString(key + "prefix", null),
                roles.getString(key + "colour", null),
                roles.getInt(key + "weight", 0)))
        );
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

//            //Sync roles.
//            Network.getInstance().getTimers().discordSync(discord_id, nRole);

        }

        //Announce the promotion in chat and discord.
        //Send a message to the user if not online, so they'll be notified of their promotion next time they join the server.
        String colouredRole = CONFIG.getString("roles." + nRole + ".colour") + CONFIG.getString("roles." + nRole + ".name");
        if (CONFIG.getBoolean("chat.announce_promotions")) {
            String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");


            Component promotation_message = Component.text(name)
                    .append(PROMOTION_TEMPLATE)
                    .append(LegacyComponentSerializer.legacyAmpersand().deserialize(colouredRole));
            promotation_message = promotation_message.decorate(TextDecoration.BOLD);

            ChatMessage chatMessage = new ChatMessage(GLOBAL.getChannelName(), "server", promotation_message);
            Network.getInstance().getChat().sendSocketMesage(chatMessage);

        }

        //Check if the player is online.
        if (!Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {

            //Send a message that will show when they next log in.
            Network.getInstance().getGlobalSQL().update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" +
                    PROMOTION_SELF + colouredRole
                    + "');");

        }
    }
}
