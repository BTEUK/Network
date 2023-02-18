package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Discord implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //If command has arg, check if it's link and sender is a player.
        if (sender instanceof Player p) {
            if (args.length > 0) {

                //Get user.
                NetworkUser user = Network.getInstance().getUser(p);

                if (args[0].equalsIgnoreCase("link")) {

                    //Check if account isn't already linked, send info to unlink.
                    if (user.isLinked) {
                        p.sendMessage(Utils.error("You are already linked, to unlink do &4/discord unlink"));
                        return true;
                    }

                    //Send random code in chat, this must be sent to the UK Bot to link your discord account.
                    //Create random code from the last 6 digits of the time.
                    String time = String.valueOf(Time.currentTime());
                    String token = time.substring(time.length() - 6);

                    Network.getInstance().chat.broadcastMessage("link " + user.player.getUniqueId() + " " + token, "uknet:discord");

                    user.player.sendMessage(Utils.success("To link your Discord please DM the code &3" + token + " &ato the UK Bot within the next 5 minutes."));
                    return true;

                } else if (args[0].equalsIgnoreCase("unlink")) {

                    //Check if account is not linked, then ask user to link first.
                    if (!user.isLinked) {
                        p.sendMessage(Utils.error("You are not linked, to link do &4/discord link"));
                        return true;
                    }

                    //Get linked discord id.
                    long discord_id = Network.getInstance().globalSQL.getLong("SELECT discord_id FROM discord WHERE uuid='" + user.player.getUniqueId() + "';");

                    //Remove linked roles from discord, then unlink.
                    //Since discord connections are handled via the proxy, get all the roles that must be unlinked and send that to the proxy with the chat socket.
                    Network.getInstance().globalSQL.update("DELETE FROM discord WHERE uuid='" + user.player.getUniqueId() + "';");
                    user.isLinked = false;

                    for (Map.Entry<String, Long> entry : Network.getInstance().timers.getRoles().entrySet()) {

                        Network.getInstance().chat.broadcastMessage("removerole " + discord_id + " " + entry.getValue(), "uknet:discord");

                    }

                    user.player.sendMessage(Utils.success("Unlinked your Discord."));
                    return true;

                }
            }
        }

        TextComponent discord = new TextComponent(Utils.chat("&aJoin our discord: &7" + Network.getInstance().getConfig().getString("discord")));
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Network.getInstance().getConfig().getString("discord")));
        sender.spigot().sendMessage(discord);

        return true;

    }
}