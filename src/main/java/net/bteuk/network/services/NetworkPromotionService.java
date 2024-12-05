package net.bteuk.network.services;

import net.bteuk.network.utils.Role;
import net.bteuk.network.utils.Roles;
import org.bukkit.entity.Player;
import teachingtutorials.services.PromotionService;

public class NetworkPromotionService implements PromotionService {
    @Override
    public void promote(Player player) {
        // If the builder role is default, promote the user.
        Role currentRole = Roles.builderRole(player);

        if (currentRole != null && currentRole.getId().equals("default")) {
            Roles.alterRole(player.getUniqueId().toString(), player.getName(), "applicant", false, true).join();
            Roles.alterRole(player.getUniqueId().toString(), player.getName(), "default", false, true).join();
        }
    }

    @Override
    public String getDescription() {
        return "Network promotion service.";
    }
}
