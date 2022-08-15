package me.bteuk.network.commands;

import me.bteuk.network.Network;
import me.bteuk.network.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Modpack implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        sender.sendMessage(Utils.chat("&aPlease select the correct version for your device:"));
        TextComponent windows = new TextComponent(Utils.chat("&aWindows (.exe): &7" + Network.getInstance().getConfig().getString("modpack.windows")));
        windows.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Network.getInstance().getConfig().getString("modpack.windows")));
        sender.spigot().sendMessage(windows);
        TextComponent mac = new TextComponent(Utils.chat("&aMacOS (.dmg): &7" + Network.getInstance().getConfig().getString("modpack.mac")));
        mac.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Network.getInstance().getConfig().getString("modpack.mac")));
        sender.spigot().sendMessage(mac);
        TextComponent linux = new TextComponent(Utils.chat("&aLinux (.AppImage): &7" + Network.getInstance().getConfig().getString("modpack.linux")));
        linux.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Network.getInstance().getConfig().getString("modpack.linux")));
        sender.spigot().sendMessage(linux);
        TextComponent universal = new TextComponent(Utils.chat("&aUniversal (.jar): &7" + Network.getInstance().getConfig().getString("modpack.universal")));
        universal.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Network.getInstance().getConfig().getString("modpack.universal")));
        sender.spigot().sendMessage(universal);

        TextComponent help = new TextComponent(Utils.chat("&aIf you need help installing the modpack you can watch this tutorial: &7" + Network.getInstance().getConfig().getString("modpack.tutorial")));
        help.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Network.getInstance().getConfig().getString("modpack.tutorial")));
        sender.spigot().sendMessage(help);

        return true;

    }
}
