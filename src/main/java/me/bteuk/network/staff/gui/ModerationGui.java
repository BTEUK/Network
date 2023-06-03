package me.bteuk.network.staff.gui;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ModerationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class ModerationGui extends Gui {

    public ModerationGui() {

        super(27, Component.text("Moderation Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        createGui();

    }

    private void createGui() {

        //Ban
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.title("Ban"),
                        Utils.line("Click to select an online user to ban.")),
                u -> openSelectUser(u, ModerationType.BAN));

        //Unban
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.title("Unban"),
                        Utils.line("Click to select a banned user to unban.")),
                u -> openSelectUser(u, ModerationType.UNBAN));

        //Mute
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.title("Mute"),
                        Utils.line("Click to select an online user to mute.")),
                u -> openSelectUser(u, ModerationType.MUTE));

        //Unmute
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.title("Unmute"),
                        Utils.line("Click to select a muted user to unmute.")),
                u -> openSelectUser(u, ModerationType.UNMUTE));

        //Kick
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.title("Kick"),
                        Utils.line("Click to select an online user to kick.")),
                u -> openSelectUser(u, ModerationType.KICK));

        //Return to moderation menu.
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Previous Page"),
                        Utils.line("Open the staff menu.")),
                u ->

                {

                    //Return to request menu.
                    this.delete();
                    u.staffGui = null;

                    u.staffGui = new StaffGui(u);
                    u.staffGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }

    private void openSelectUser(NetworkUser u, ModerationType type) {

        //Switch to the select user menu.
        this.delete();

        u.staffGui = new SelectUser(type);
        u.staffGui.open(u);

    }
}
