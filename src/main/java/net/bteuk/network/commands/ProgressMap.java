package net.bteuk.network.commands;

import net.bteuk.network.Network;
import static net.bteuk.network.utils.Constants.PROGRESS_MAP;
import net.bteuk.network.utils.NetworkUser;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProgressMap implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        //Check if the sender is a player.
        if (!(commandSender instanceof Player p)) {

            commandSender.sendMessage(Utils.error("This command can only be used by a player."));
            return true;

        }

        //Get user.
        NetworkUser u = Network.getInstance().getUser(p);

        if (u == null) {return true;}

        //Send them a link
        if (PROGRESS_MAP)
        {
            TextComponent textComponent = Component.text("Click here to view a map of our progress!", NamedTextColor.AQUA);
            textComponent = textComponent.clickEvent(ClickEvent.openUrl(Network.getInstance().getConfig().getString("ProgressMap.Link")));
            u.player.sendMessage(textComponent);
        }

        return true;
    }

}
