package me.bteuk.network.gui.staff;

import lombok.Getter;
import lombok.Setter;
import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.listeners.staff.ModerationReasonListener;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ModerationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.Locale;

import static me.bteuk.network.utils.enums.ModerationType.BAN;

public class ModerationActionGui extends Gui {

    @Setter
    private String reason;

    @Getter
    private final ModerationType type;

    private final String uuid;

    private final String name;

    private ModerationReasonListener reasonListener;

    private int hours = 0;
    private int days = 0;
    private int months = 0;
    private int years = 0;

    public ModerationActionGui(ModerationType type, String uuid) {

        super(9, Component.text(type.label + " Menu", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.type = type;
        this.uuid = uuid;

        name = Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';");

        createGui();

    }

    private void createGui() {

        //Unregister the listener if it has not been already.
        if (reasonListener != null) {
            reasonListener.getTask().cancel();
            reasonListener.unregister();
        }

        switch (type) {

            case KICK ->

                    //Kick
                    setItem(0, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                                    Utils.title("Kick " + name),
                                    Utils.line("Kick the player with the specified reason.")),
                            u -> {

                                //If a reason has been set, kick the user, if they're still online.
                                if (Network.getInstance().getGlobalSQL().hasRow("SELECT uuid FROM online_users WHERE uuid='" + uuid + "';")) {
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

            case BAN, MUTE -> {

                //Time selection buttons.
                setItem(3, Utils.createItem((hours == 0) ? Material.BARRIER : Material.CLOCK, (hours == 0) ? 1 : hours,
                                Utils.title("Hours"),
                                Utils.line("Click to increase the hours by 1.")),
                        u -> {

                            hours = (hours == 24) ? 0 : hours + 1;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                        });

                setItem(4, Utils.createItem((days == 0) ? Material.BARRIER : Material.CLOCK, (days == 0) ? 1 : days,
                                Utils.title("Days"),
                                Utils.line("Click to increase the hours by 1.")),
                        u -> {

                            days = (days == 30) ? 0 : days + 1;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                        });

                setItem(5, Utils.createItem((months == 0) ? Material.BARRIER : Material.CLOCK, (months == 0) ? 1 : months,
                                Utils.title("Months"),
                                Utils.line("Click to increase the hours by 1.")),
                        u -> {

                            months = (months == 12) ? 0 : months + 1;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                        });

                setItem(6, Utils.createItem((years == 0) ? Material.BARRIER : Material.CLOCK, (years == 0) ? 1 : years,
                                Utils.title("Years"),
                                Utils.line("Click to increase the hours by 1.")),
                        u -> {

                            years = (years == 5) ? 0 : years + 1;
                            this.refresh();
                            u.player.getOpenInventory().getTopInventory().setContents(getInventory().getContents());

                        });

                //Ban/Mute
                setItem(0, Utils.createItem(Material.REDSTONE_BLOCK, 1,
                                Utils.title(type.label + " " + name),
                                Utils.line(type.label + " the player with the specified reason and time.")),
                        u -> {

                            //Check if the reason is set.
                            if (reason != null) {

                                //Check if time is set.
                                if (hours > 0 || days > 0 || months > 0 || years > 0) {

                                    //Get time.
                                    long end_time = Time.currentTime() + Time.toMilliseconds(hours, days, months, years);

                                    if (type == BAN) {

                                        //Ban the user.
                                        u.player.sendMessage(Network.getInstance().getBan().banPlayer(name, uuid, end_time, reason));

                                    } else {

                                        //Mute the user.
                                        u.player.sendMessage(Network.getInstance().getMute().mutePlayer(name, uuid, end_time, reason));

                                    }

                                    //Close inventory and clear gui (this means it'll open as a staff menu next time).
                                    this.delete();
                                    u.staffGui = null;

                                    u.player.closeInventory();

                                } else {

                                    u.player.sendMessage(Utils.error("You must provide a duration to " + type.label.toLowerCase(Locale.ROOT) + " someone."));
                                    u.player.closeInventory();

                                }

                            } else {

                                u.player.sendMessage(Utils.error("You must provide a reason to " + type.label.toLowerCase(Locale.ROOT) + " someone."));
                                u.player.closeInventory();

                            }

                        });
            }

        }

        //Set reason
        setItem(2, Utils.createItem(Material.WRITABLE_BOOK, 1,
                        Utils.title("Reason"),
                        Utils.line("Click to write the reason in chat."),
                        Utils.line("Your first chat message will be set as reason."),
                        Utils.line("If you don't type anything within 1 minute"),
                        Utils.line("the action gets cancelled.")),
                u ->

                {

                    //Prompt the user for a reason.
                    reasonListener = new ModerationReasonListener(u, this);

                    u.player.closeInventory();
                    u.player.sendMessage(Utils.success("Please write the reason in chat, the first message counts."));

                });

        //Return to select user menu.
        setItem(8, Utils.createItem(Material.SPRUCE_DOOR, 1,
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
