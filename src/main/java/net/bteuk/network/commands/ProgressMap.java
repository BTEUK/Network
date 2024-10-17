package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.bteuk.network.utils.Constants.PROGRESS_MAP;
import static net.bteuk.network.utils.NetworkConfig.CONFIG;

public class ProgressMap extends AbstractCommand {
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        //Send them a link
        if (PROGRESS_MAP) {
            TextComponent textComponent = Component.text("Click here to view a map of our progress!", NamedTextColor.AQUA);
            textComponent = textComponent.clickEvent(ClickEvent.openUrl(CONFIG.getString("ProgressMap.Link", Strings.EMPTY)));
            player.sendMessage(textComponent);
        }
    }
}
