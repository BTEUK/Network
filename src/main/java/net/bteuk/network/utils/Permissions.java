package net.bteuk.network.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.buildtheearth.terraminusminus.TerraMinusMinus.LOGGER;

/**
 * Utility class for permission related actions.
 */
public final class Permissions {

    private Permissions() {}

    private static LuckPerms getProvider() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

        return provider == null ? null : provider.getProvider();
    }

    /**
     * Checks whether a group exists with the name provided.
     * @param groupName the name of the group to check
     * @return {@link Group}
     */
    public static Group getGroup(String groupName) {
        LuckPerms provider = getProvider();

        if (provider == null) {
            LOGGER.warn("LuckPerms is required, but not available!");
            return null;
        }

        return provider.getGroupManager().getGroup(groupName);
    }

    public static CompletableFuture<Boolean> modifyGroup(String uuid, Group group, boolean remove) {
        LuckPerms provider = getProvider();

        if (provider == null) {
            return null;
        }

        UserManager userManager = provider.getUserManager();

        // Add group to user. Returns boolean to indicate success.
        return userManager.loadUser(UUID.fromString(uuid)).thenApplyAsync(user -> {
            InheritanceNode node = InheritanceNode.builder(group).build();
            DataMutateResult result;
            if (remove) {
                result = user.data().remove(node);
            } else {
                result = user.data().add(node);
            }
            // Save the user if successful.
            if (result.wasSuccessful()) {
                userManager.saveUser(user);
            }

            return result.wasSuccessful();
        });
    }

    public static CompletableFuture<String> getPrimaryGroup(String uuid) {
        LuckPerms provider = getProvider();

        if (provider == null) {
            return null;
        }

        UserManager userManager = provider.getUserManager();
        return userManager.loadUser(UUID.fromString(uuid)).thenApplyAsync(User::getPrimaryGroup);
    }
}
