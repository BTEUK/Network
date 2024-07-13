package net.bteuk.network.utils;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.ChatMessage;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.dto.DiscordRole;
import net.bteuk.network.lib.dto.TabPlayer;
import net.bteuk.network.lib.dto.UserUpdate;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import static net.bteuk.network.lib.enums.ChatChannels.GLOBAL;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public final class Roles {

    private static final Component PROMOTION_TEMPLATE = Component.text(" has been promoted to ");
    private static final Component DEMOTION_TEMPLATE = Component.text(" has been demoted from ");
    private static final Component PROMOTION_SELF = Component.text("You have been promoted to ");

    private static final Component DEMOTION_SELF = Component.text("You have been demoted from ");

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
                roles.getString(key + ".name", null),
                roles.getString(key + ".prefix", null),
                roles.getString(key + ".colour", null),
                roles.getInt(key + ".weight", 0)))
        );
    }

    /**
     * Discord syncing will not be applied with this method due to deprecation.
     */
    @Deprecated
    public static void promoteBuilder(String uuid, String pRole, String nRole) {

        //Get console sender.
        ConsoleCommandSender console = Network.getInstance().getServer().getConsoleSender();

        //Add new builder role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent add " + nRole);

        //Remove current builder role. Remove after adding to make sure the player always has a role.
        Bukkit.getServer().dispatchCommand(console, "lp user " + uuid + " parent remove " + pRole);

        //Update database.
        Network.getInstance().getGlobalSQL().update("UPDATE player_data SET builder_role='" + nRole + "' WHERE uuid='" + uuid + "';");

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
                    PlainTextComponentSerializer.plainText().serialize(PROMOTION_SELF) + colouredRole
                    + "');");

        }
    }

    /**
     * Promote/demote a player for a specific role.
     * @param uuid the uuid of the player to promote.
     * @param roleId the role to add or remove
     * @param remove whether to remove the role or not
     * @param announce whether to announce the promotion/demotion
     * @return {@link CompletableFuture} completableFuture with {@link Component} message.
     */
    public static CompletableFuture<Component> alterRole(String uuid, String name, String roleId, boolean remove, boolean announce) {

        // Get the configured group.
        Role role = getRoleById(roleId);
        Group group = Permissions.getGroup(roleId);

        if (group == null || role == null) {
            return CompletableFuture.completedFuture(ChatUtils.error("%s is not configured in LuckPerms and/or roles.yml.", roleId));
        }

        return CompletableFuture.supplyAsync(() -> {

            CompletableFuture<String> groupBeforeFuture = Permissions.getPrimaryGroup(uuid);

            if (groupBeforeFuture == null) {
                return ChatUtils.error("No primary group could be found for this user.");
            }

            String groupBefore = groupBeforeFuture.join();

            CompletableFuture<Boolean> booleanFuture = Permissions.modifyGroup(uuid, group, remove);

            if (booleanFuture == null) {
                return ChatUtils.error("Modifying the permissions failed!");
            }

            boolean success = booleanFuture.join();

            if (!success) {
                return ChatUtils.error("Modifying the permissions failed!");
            }

            CompletableFuture<String> groupAfterFuture = Permissions.getPrimaryGroup(uuid);

            if (groupAfterFuture == null) {
                return ChatUtils.error("No primary group could be found for this user.");
            }

            String groupAfter = groupAfterFuture.join();

            if (!groupBefore.equals(groupAfter)) {
                // Update primary role in TAB.
                Role primaryRole = getRoleById(groupAfter);
                TabPlayer tabPlayer = new TabPlayer();
                tabPlayer.setUuid(uuid);
                tabPlayer.setName(name);
                tabPlayer.setPrimaryGroup(primaryRole.getId());
                tabPlayer.setPrefix(primaryRole.getColouredPrefix());
                UserUpdate userUpdate = new UserUpdate();
                userUpdate.setUuid(uuid);
                userUpdate.setTabPlayer(tabPlayer);
                Network.getInstance().getChat().sendSocketMesage(userUpdate);
            }

            DiscordRole discordRole = new DiscordRole(uuid, roleId, !remove);
            Network.getInstance().getChat().sendSocketMesage(discordRole);

            if (announce) {
                ChatMessage chatMessage = getPromotionChatMessage(remove, name, role);
                Network.getInstance().getChat().sendSocketMesage(chatMessage);
            }

            DirectMessage directMessage = getPromotionDirectMessage(remove, uuid, role);
            Network.getInstance().getChat().sendSocketMesage(directMessage);

            if (remove) {
                return ChatUtils.success("Demoted %s from %s", name, roleId);
            } else {
                return ChatUtils.success("Promoted %s to %s", name, roleId);
            }
        });
    }

    private static ChatMessage getPromotionChatMessage(boolean remove, String name, Role role) {
        Component message;
        if (remove) {
            message = Component.text(name)
                    .append(DEMOTION_TEMPLATE)
                    .append(role.getColouredRoleName());
        } else {
            message = Component.text(name)
                    .append(PROMOTION_TEMPLATE)
                    .append(role.getColouredRoleName());
        }
        message = message.decorate(TextDecoration.BOLD);
        return new ChatMessage(GLOBAL.getChannelName(), "server", message);
    }

    private static DirectMessage getPromotionDirectMessage(boolean remove, String uuid, Role role) {
        Component message;
        if (remove) {
            message = DEMOTION_SELF
                    .append(role.getColouredRoleName());
        } else {
            message = PROMOTION_TEMPLATE
                    .append(role.getColouredRoleName());
        }
        message = message.decorate(TextDecoration.BOLD);
        return new DirectMessage(uuid, "server", message, true);
    }

    private static Role getRoleById(String roleId) {
        return ROLES.stream().filter(role -> role.getId().equalsIgnoreCase(roleId)).findFirst().orElse(null);
    }
}
