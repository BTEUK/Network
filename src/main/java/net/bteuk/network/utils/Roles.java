package net.bteuk.network.utils;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.ChatMessage;
import net.bteuk.network.lib.dto.DirectMessage;
import net.bteuk.network.lib.dto.DiscordRole;
import net.bteuk.network.lib.dto.TabPlayer;
import net.bteuk.network.lib.dto.UserUpdate;
import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.bteuk.network.lib.enums.ChatChannels.GLOBAL;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public final class Roles {

    private static final Component PROMOTION_TEMPLATE = Component.text(" has been promoted to ");
    private static final Component PROMOTION_SELF = Component.text("You have been promoted to ");

    private static Set<Role> ROLES;

    private static final LinkedHashSet<String> BUILDER_ROLE_NAMES = Stream.of("reviewer", "architect", "builder", "jrbuilder", "apprentice", "applicant", "default")
            .collect(Collectors.toCollection(LinkedHashSet::new));

    public static Set<Role> getRoles() {
        if (ROLES == null) {
            loadRoles();
        }
        return ROLES;
    }

    public static Role getRoleById(String roleId) {
        // Get the configuration if not yet fetches.
        if (ROLES == null) {
            loadRoles();
        }
        return ROLES.stream().filter(role -> role.getId().equalsIgnoreCase(roleId)).findFirst().orElse(null);
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
    public static Role builderRole(Player p) {
        String roleToGet = "default";
        for (String roleName : BUILDER_ROLE_NAMES) {
            if (p.hasPermission("group." + roleName)) {
                roleToGet = roleName;
                break;
            }
        }
        return getRoleById(roleToGet);
    }

    /**
     * Get the builder role for a potentially offline player.
     * @param uuid the uuid of the player
     * @return a {@link CompletableFuture} with a String
     */
    public static CompletableFuture<String> builderRole(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            CompletableFuture<Boolean> isReviewer = Permissions.hasGroup(uuid, "reviewer");
            CompletableFuture<Boolean> isArchitect = Permissions.hasGroup(uuid, "architect");
            CompletableFuture<Boolean> isBuilder = Permissions.hasGroup(uuid, "builder");
            CompletableFuture<Boolean> isJrbuilder = Permissions.hasGroup(uuid, "jrbuilder");
            CompletableFuture<Boolean> isApprentice = Permissions.hasGroup(uuid, "apprentice");
            CompletableFuture<Boolean> isApplicant = Permissions.hasGroup(uuid, "applicant");
            if (isReviewer != null && isReviewer.join()) {
                return "reviewer";
            } else if (isArchitect != null && isArchitect.join()) {
                return "architect";
            } else if (isBuilder != null && isBuilder.join()) {
                return "builder";
            } else if (isJrbuilder != null && isJrbuilder.join()) {
                return "jrbuilder";
            } else if (isApprentice != null && isApprentice.join()) {
                return "apprentice";
            } else if (isApplicant != null && isApplicant.join()) {
                return "applicant";
            } else {
                return "default";
            }
        });
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
        Component colouredRole = Component.text(Objects.requireNonNull(CONFIG.getString("roles." + nRole + ".name")), TextColor.fromHexString(Objects.requireNonNull(CONFIG.getString("roles." + nRole + ".colour"))));
        if (CONFIG.getBoolean("chat.announce_promotions")) {
            String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");

            Component promotation_message = Component.text(name)
                    .append(PROMOTION_TEMPLATE)
                    .append(colouredRole);
            promotation_message = promotation_message.decorate(TextDecoration.BOLD);

            ChatMessage chatMessage = new ChatMessage(GLOBAL.getChannelName(), "server", promotation_message);
            Network.getInstance().getChat().sendSocketMesage(chatMessage);

        }

        //Check if the player is online.
        if (!Network.getInstance().isOnlineOnNetwork(uuid)) {

            //Send a message that will show when they next log in.
            DirectMessage directMessage = new DirectMessage(uuid, "server",
                    PROMOTION_SELF.append(colouredRole),
                    true);
            Network.getInstance().getChat().sendSocketMesage(directMessage);

        }
    }

    /**
     * Promote/demote a player for a specific role.
     * @param uuid the uuid of the player to promote.
     * @param roleId the role to add or remove
     * @param remove whether to remove the role or not
     * @param announce whether to announce the promotion (demotion is never announced)
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

            String groupBefore;
            try {
                groupBefore = Objects.requireNonNull(Permissions.getPrimaryGroup(uuid)).join();
            } catch (Exception e) {
                return ChatUtils.error("An error occurred while fetching the primary group.");
            }

            String groupAfter = Permissions.modifyGroup(uuid, group, remove);

            if (groupAfter == null) {
                return ChatUtils.error("Modifying the permissions failed!");
            }

            LOGGER.info(String.format("Group before %s, group after %s", groupBefore, groupAfter));
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

            if (announce && !remove) {
                sendPromotionChatMessage(name, role);
            }

            if (!remove) {
                sendPromotionDirectMessage(uuid, role);
            }

            if (remove) {
                return ChatUtils.success("Demoted %s from %s", name, roleId);
            } else {
                return ChatUtils.success("Promoted %s to %s", name, roleId);
            }
        });
    }

    private static void sendPromotionChatMessage(String name, Role role) {
        Component message = Component.text(name)
                .append(PROMOTION_TEMPLATE)
                .append(role.getColouredRoleName())
                .decorate(TextDecoration.BOLD);
        Network.getInstance().getChat().sendSocketMesage(new ChatMessage(GLOBAL.getChannelName(), "server", message));
    }

    private static void sendPromotionDirectMessage(String uuid, Role role) {
        Component message = PROMOTION_SELF
                .append(role.getColouredRoleName())
                .decorate(TextDecoration.BOLD);
        Network.getInstance().getChat().sendSocketMesage(new DirectMessage(uuid, "server", message, true));
    }
}
