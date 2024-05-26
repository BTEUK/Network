package net.bteuk.network.commands;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.DiscordLinking;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Time;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.bteuk.network.utils.Constants.DISCORD_LINKING;
import static net.bteuk.network.utils.Constants.LOGGER;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class Discord implements CommandExecutor {

    @Deprecated
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //If command has arg, check if it's link and sender is a player.
        if (sender instanceof Player p) {
            if (args.length > 0) {

                //Get user.
                NetworkUser user = Network.getInstance().getUser(p);

                //Check if user is null.
                if (user == null) {

                    LOGGER.severe("User " + p.getName() + " is null, command " + command.getName() + " can't be executed!");
                    LOGGER.severe("This will also impact all other Network-related functions.");

                    p.sendMessage(ChatUtils.error("An error occurred, please contact a server admin!"));
                    return true;

                }

                //If discord linking is enabled
                if (DISCORD_LINKING) {
                    if (args[0].equalsIgnoreCase("link")) {

                        //Check if account isn't already linked, send info to unlink.
                        if (user.isLinked) {
                            p.sendMessage(ChatUtils.error("You are already linked, to unlink do %s", "/discord unlink"));
                            return true;
                        }

                        //Send random code in chat, this must be sent to the UK Bot to link your discord account.
                        //Create random code from the last 6 digits of the time.
                        String time = String.valueOf(Time.currentTime());
                        String token = time.substring(time.length() - 6);

                        DiscordLinking discordLinking = new DiscordLinking();
                        discordLinking.setUuid(p.getUniqueId().toString());
                        discordLinking.setToken(token);
                        
                        Network.getInstance().getChat().sendSocketMesage(discordLinking);

                        p.sendMessage(ChatUtils.success("To link your Discord please DM the code %s to the UK Bot within the next 5 minutes.", token));
                        return true;

                    } else if (args[0].equalsIgnoreCase("unlink")) {

                        //Check if account is not linked, then ask user to link first.
                        if (!user.isLinked) {
                            p.sendMessage(ChatUtils.error("You are not linked, to link do %s", "/discord link"));
                            return true;
                        }

                        //Remove linked roles from discord, then unlink.
                        //Since discord connections are handled via the proxy, get all the roles that must be unlinked and send that to the proxy with the chat socket.
                        DiscordLinking discordLinking = new DiscordLinking();
                        discordLinking.setUuid(p.getUniqueId().toString());
                        discordLinking.setDiscordId(user.getDiscordId());
                        discordLinking.setUnlink(true);
                        
                        user.isLinked = false;
                        p.sendMessage(ChatUtils.success("Unlinked your Discord."));
                        return true;

                    }
                }
            }
        }

        Component discord = ChatUtils.success("Join our discord: " + CONFIG.getString("discord"));
        discord = discord.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, Objects.requireNonNull(CONFIG.getString("discord"))));
        sender.sendMessage(discord);

        return true;

    }
}