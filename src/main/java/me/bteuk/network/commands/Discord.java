package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

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

                    p.sendMessage(Utils.error("An error occurred, please contact a server admin!"));
                    return true;

                }

                if (args[0].equalsIgnoreCase("link")) {

                    //Check if account isn't already linked, send info to unlink.
                    if (user.isLinked) {
                        p.sendMessage(Utils.error("You are already linked, to unlink do ")
                                .append(Component.text("/discord unlink", NamedTextColor.DARK_RED)));
                        return true;
                    }

                    //Send random code in chat, this must be sent to the UK Bot to link your discord account.
                    //Create random code from the last 6 digits of the time.
                    String time = String.valueOf(Time.currentTime());
                    String token = time.substring(time.length() - 6);

                    Network.getInstance().chat.broadcastMessage(Component.text("link " + user.player.getUniqueId() + " " + token), "uknet:discord_linking");

                    user.player.sendMessage(Utils.success("To link your Discord please DM the code ")
                            .append(Component.text(token, NamedTextColor.DARK_AQUA))
                            .append(Utils.success(" to the UK Bot within the next 5 minutes.")));
                    return true;

                } else if (args[0].equalsIgnoreCase("unlink")) {

                    //Check if account is not linked, then ask user to link first.
                    if (!user.isLinked) {
                        p.sendMessage(Utils.error("You are not linked, to link do ")
                                .append(Component.text("/discord link", NamedTextColor.DARK_RED)));
                        return true;
                    }

                    //Get linked discord id.
                    long discord_id = Network.getInstance().globalSQL.getLong("SELECT discord_id FROM discord WHERE uuid='" + user.player.getUniqueId() + "';");

                    //Remove linked roles from discord, then unlink.
                    //Since discord connections are handled via the proxy, get all the roles that must be unlinked and send that to the proxy with the chat socket.
                    Network.getInstance().globalSQL.update("DELETE FROM discord WHERE uuid='" + user.player.getUniqueId() + "';");
                    user.isLinked = false;

                    for (Map.Entry<String, Long> entry : Network.getInstance().timers.getRoles().entrySet()) {

                        Network.getInstance().chat.broadcastMessage(Component.text("removerole " + discord_id + " " + entry.getValue()), "uknet:discord_linking");

                    }

                    user.player.sendMessage(Utils.success("Unlinked your Discord."));
                    return true;

                }
            }
        }

        Component discord = Utils.success("Join our discord: " + CONFIG.getString("discord"));
        discord = discord.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, Objects.requireNonNull(CONFIG.getString("discord"))));
        sender.sendMessage(discord);

        return true;

    }
}