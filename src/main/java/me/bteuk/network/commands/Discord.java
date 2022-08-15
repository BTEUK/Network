package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Discord implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        TextComponent discord = new TextComponent(Utils.chat("&aJoin our discord: &7" + Network.getInstance().getConfig().getString("discord")));
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Network.getInstance().getConfig().getString("discord")));
        sender.spigot().sendMessage(discord);

        return true;

    }
}