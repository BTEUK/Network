package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.commands.tabcompleters.PlayerSelector;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Phead extends AbstractCommand {

    //Constructor to enable the command.
    public Phead() {
        //Set tab completer.
        setTabCompleter(new PlayerSelector(false));
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {

        //Check if the sender is a player.
        Player player = getPlayer(stack);
        if (player == null) {
            return;
        }

        //Check if the player has permission.
        if (!hasPermission(player, "uknet.phead")) {
            return;
        }

        //Check if there is at least 1 arg.
        if (args.length < 1) {
            player.sendMessage(ChatUtils.error("/phead <name>"));
            return;
        }

        //Check if player exists in the database.
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM player_data WHERE name='" + args[0] + "';")) {

            //Get uuid.
            String uuid = Network.getInstance().getGlobalSQL().getString("SELECT uuid FROM player_data WHERE name='" + args[0] + "';");
            String name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE name='" + args[0] + "';");

            //Give item to player.
            Utils.giveItem(player, Utils.createPlayerSkull(uuid, 1, Component.text(name + "'s head", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)), name + "'s head");

        } else {

            player.sendMessage(Component.text(args[0], NamedTextColor.DARK_RED).append(ChatUtils.error(" could not be found.")));

        }
    }
}
