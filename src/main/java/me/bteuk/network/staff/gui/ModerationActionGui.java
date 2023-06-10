package me.bteuk.network.staff.gui;

import lombok.Getter;
import lombok.Setter;
import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.staff.listeners.ModerationReasonListener;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ModerationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class ModerationActionGui extends Gui {

    @Setter
    private String reason;

    @Getter
    private final ModerationType type;

    private final String uuid;

    private final String name;

    private ModerationReasonListener reasonListener;

    public ModerationActionGui(ModerationType type, String uuid) {

        super(27, Component.text(type.label + " Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.type = type;
        this.uuid = uuid;

        name = Network.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");

        createGui();

    }

    private void createGui() {

        //Unregister the listener if it has not been already.
        if (reasonListener != null) {
            reasonListener.getTask().cancel();
            reasonListener.unregister();
        }

        switch (type) {

            case KICK -> {

                //Kick
                setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                                Utils.title("Kick " + name),
                                Utils.line("Kick the player with the specified reason.")),
                        u -> {

                            //If a reason has been set, kick the user, if they're still online.
                            if (Network.getInstance().globalSQL.hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {
                                if (reason != null) {

                                    //Kick the user.
                                    u.player.sendMessage(Network.getInstance().getKick().kickPlayer(name, uuid, reason));

                                    //Close inventory and clear gui (this means it'll open as a staff menu next time).
                                    this.delete();
                                    u.staffGui = null;

                                    u.player.closeInventory();

                                } else {

                                    u.player.sendMessage(Utils.error("You must provide a reason to kick someone."));
                                    u.player.closeInventory();

                                }
                            } else {

                                u.player.sendMessage(Component.text(name, NamedTextColor.DARK_RED).append(Utils.error(" is no longer online.")));

                                //Close inventory and clear gui (this means it'll open as a staff menu next time).
                                this.delete();
                                u.staffGui = null;

                                u.player.closeInventory();

                            }
                        });
            }

            case BAN -> {

            }

            case MUTE -> {

            }

        }

        //Set reason
        setItem(16, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                        Utils.title("Reason"),
                        Utils.line("Click to write the reason in chat."),
                        Utils.line("Your first chat message will be set as reason."),
                        Utils.line("If you don't type anything within 1 minute"),
                        Utils.line("the action gets cancelled.")),
                u -> {

                    //Prompt the user for a reason.
                    reasonListener = new ModerationReasonListener(u, this);

                    u.player.closeInventory();
                    u.player.sendMessage("Please write the reason in chat, the first message counts.");

                });

        //Return to select user menu.
        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.title("Previous Page"),
                        Utils.line("Open the select user menu.")),
                u ->

                {

                    //Return to request menu.
                    this.delete();
                    u.staffGui = new SelectUser(type);
                    u.staffGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
