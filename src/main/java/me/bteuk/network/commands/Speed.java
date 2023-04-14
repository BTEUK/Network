package me.bteuk.network.commands;

import me.bteuk.network.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Speed implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player.
        if (!(sender instanceof Player p)) {

            sender.sendMessage(Utils.error("This command can only be run by a player."));
            return true;

        }

        boolean isFly = p.isFlying();

        if (args.length < 1) {
            error(p);
            return true;
        }

        float speed;

        try {
            speed = getMoveSpeed(args[0]);
        } catch (NumberFormatException e) {
            error(p);
            return true;
        }

        speed = getRealMoveSpeed(speed, isFly);

        if (isFly) {

            p.setFlySpeed(speed);
            p.sendMessage(Utils.success("Set flying speed to ")
                    .append(Component.text(args[0], NamedTextColor.DARK_AQUA)));

        } else {

            p.setWalkSpeed(speed);
            p.sendMessage(Utils.success("Set walking speed to ")
                    .append(Component.text(args[0], NamedTextColor.DARK_AQUA)));

        }

        return true;
    }

    //Returns the move speed from a string.
    private float getMoveSpeed(final String moveSpeed) throws NumberFormatException {
        float userSpeed;
        userSpeed = Float.parseFloat(moveSpeed);
        if (userSpeed > 10f) {
            userSpeed = 10f;
        } else if (userSpeed < 0.0001f) {
            userSpeed = 0.0001f;
        }
        return userSpeed;
    }

    //Converts the move speed to a Minecraft usable value.
    private float getRealMoveSpeed(final float userSpeed, final boolean isFly) {
        final float defaultSpeed = isFly ? 0.1f : 0.2f;
        float maxSpeed = 1f;

        if (userSpeed < 1f) {
            return defaultSpeed * userSpeed;
        } else {
            final float ratio = ((userSpeed - 1) / 9) * (maxSpeed - defaultSpeed);
            return ratio + defaultSpeed;
        }
    }

    //Error message.
    private void error(Player p) {
        p.sendMessage(Utils.error("/speed [0-10]"));
    }
}
