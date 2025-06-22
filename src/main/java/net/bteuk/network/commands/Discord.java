package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.FixedArgSelector;
import net.bteuk.network.lib.dto.DiscordLinking;
import net.bteuk.network.lib.dto.DiscordRole;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Role;
import net.bteuk.network.utils.Roles;
import net.bteuk.network.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class Discord extends AbstractCommand {

    public Discord() {
        setTabCompleter(new FixedArgSelector(Arrays.asList("link", "unlink"), 0));
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        // Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        if (args.length > 0) {

            NetworkUser user = Network.getInstance().getUser(player);

            // If u is null, cancel.
            if (user == null) {
                LOGGER.severe("User " + player.getName() + " can not be found!");
                player.sendMessage(ChatUtils.error("User can not be found, please relog!"));
                return;
            }

            // If discord linking is enabled
            if (args[0].equalsIgnoreCase("link")) {

                // Check if account isn't already linked, send info to unlink.
                if (user.isLinked) {
                    player.sendMessage(ChatUtils.error("You are already linked, to unlink do %s", "/discord unlink"));
                    return;
                }

                // Send random code in chat, this must be sent to the UK Bot to link your discord account.
                // Create random code from the last 6 digits of the time.
                String time = String.valueOf(Time.currentTime());
                String token = time.substring(time.length() - 6);

                DiscordLinking discordLinking = new DiscordLinking();
                discordLinking.setUuid(player.getUniqueId().toString());
                discordLinking.setToken(token);

                Network.getInstance().getChat().sendSocketMesage(discordLinking);

                player.sendMessage(ChatUtils.success("To link your Discord please DM the code %s to the UK Bot within" +
                        " the next 5 minutes.", token));
                return;
            } else if (args[0].equalsIgnoreCase("unlink")) {

                // Check if account is not linked, then ask user to link first.
                if (!user.isLinked) {
                    player.sendMessage(ChatUtils.error("You are not linked, to link do %s", "/discord link"));
                    return;
                }

                // Remove linked roles from discord, then unlink.
                Role role = Roles.builderRole(user.player);

                // Remove the role in discord.
                if (role == null) {
                    user.sendMessage(ChatUtils.error("You have an invalid role, please contact an administrator."));
                    return;
                }

                DiscordRole discordRole = new DiscordRole(user.player.getUniqueId().toString(), role.getId(), false);
                Network.getInstance().getChat().sendSocketMesage(discordRole);

                DiscordLinking discordLinking = new DiscordLinking();
                discordLinking.setUuid(player.getUniqueId().toString());
                discordLinking.setDiscordId(user.getDiscordId());
                discordLinking.setUnlink(true);
                Network.getInstance().getChat().sendSocketMesage(discordLinking);

                user.isLinked = false;
                player.sendMessage(ChatUtils.success("Unlinked your Discord."));
                return;
            }
        }

        Component discord = ChatUtils.success("Join our discord: " + CONFIG.getString("discord"));
        discord = discord.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL,
                Objects.requireNonNull(CONFIG.getString("discord"))));
        stack.getSender().sendMessage(discord);
    }
}