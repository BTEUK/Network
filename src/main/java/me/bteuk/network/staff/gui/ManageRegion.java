package me.bteuk.network.staff.gui;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.RegionStatus;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class ManageRegion extends Gui {

    private final Region region;
    private final NetworkUser user;

    public ManageRegion(NetworkUser user, Region region) {

        super(27, Component.text("Manage Region", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = region;
        this.user = user;

        createGui();

    }

    private void createGui() {

        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.title("Region " + region.regionName()),
                Utils.line("Current owner: ")
                        .append(Component.text(region.ownerName(), NamedTextColor.GRAY)),
                Utils.line("Number of members: ")
                        .append(Component.text(region.memberCount(), NamedTextColor.GRAY)),
                Utils.line("Region status: ")
                        .append(Component.text(region.status().label, NamedTextColor.GRAY))));


        //Set public if status is default or inactive.
        //Set private if status is public.
        if (user.player.hasPermission("uknet.regions.manage.public")) {
            if (region.status() == RegionStatus.DEFAULT || region.status() == RegionStatus.INACTIVE) {

                setItem(11, Utils.createItem(Material.OAK_DOOR, 1,
                                Utils.title("Make region public"),
                                Utils.line("A public region allows members"),
                                Utils.line("to join without needing"),
                                Utils.line("the owner to accept it.")),

                        u -> {

                            region.setPublic();
                            this.refresh();

                        });

            } else if (region.status() == RegionStatus.PUBLIC) {

                setItem(11, Utils.createItem(Material.IRON_DOOR, 1,
                                Utils.title("Make region private"),
                                Utils.line("The default region setting,"),
                                Utils.line("joining requires the owner"),
                                Utils.line("to accept the request.")),

                        u -> {

                            region.setDefault();
                            this.refresh();

                        });
            }
        }

        //Transfer ownership if status is default or public, must have at least 1 member.
        if (user.player.hasPermission("uknet.regions.manage.owner")) {
            if (region.hasMember()) {

                //Slot 14
                setItem(14, Utils.createItem(Material.MAGENTA_GLAZED_TERRACOTTA, 1,
                                Utils.title("Transfer Ownership"),
                                Utils.line("Open the transfer ownership menu."),
                                Utils.line("Allows you to make a member"),
                                Utils.line("the new region owner.")),

                        u -> {

                            //Close this menu.
                            this.delete();
                            u.staffGui = null;

                            //Open transfer owner menu.
                            u.staffGui = new TransferOwner(region);
                            u.staffGui.open(u);


                        });
            }
        }

        //Kick members, must have owner and/or members.
        if (user.player.hasPermission("uknet.regions.manage.kick")) {
            if (region.hasOwner() || region.hasMember()) {

                //Slot 15
                setItem(15, Utils.createItem(Material.BARRIER, 1,
                                Utils.title("Kick Members"),
                                Utils.line("Remove any current members."),
                                Utils.line("or the owner from the region.")),

                        u -> {

                            //Close this menu.
                            this.delete();
                            u.staffGui = null;

                            //Open transfer owner menu.
                            u.staffGui = new KickMembers(region);
                            u.staffGui.open(u);


                        });
            }
        }

        //Set region locked if region is default, public, open or inactive.
        //Set region unlocked if region is locked.
        if (user.player.hasPermission("uknet.regions.manage.lock")) {
            if (region.status() == RegionStatus.DEFAULT || region.status() == RegionStatus.PUBLIC || region.status() == RegionStatus.OPEN || region.status() == RegionStatus.INACTIVE) {

                setItem(12, Utils.createItem(Material.IRON_TRAPDOOR, 1,
                                Utils.title("Lock Region"),
                                Utils.line("Locking a region stops anyone"),
                                Utils.line("from joining or building in the"),
                                Utils.line("region, any existing members"),
                                Utils.line("will be kicked")),

                        u -> {

                            //If region is currently open, remove jrbuilder group.
                            if (region.status() == RegionStatus.OPEN) {
                                region.setDefault("jrbuilder");
                            }

                            region.setLocked();

                            this.refresh();

                        });

            } else if (region.status() == RegionStatus.LOCKED) {

                setItem(12, Utils.createItem(Material.OAK_TRAPDOOR, 1,
                                Utils.title("Unlock Region"),
                                Utils.line("The default region setting,"),
                                Utils.line("people will be able to join"),
                                Utils.line("and building in the region again.")),

                        u -> {

                            region.setDefault();
                            this.refresh();

                        });
            }
        }

        //Set region open if status is default, public or inactive.
        //Set region default if status is open.
        if (user.player.hasPermission("uknet.regions.manage.open")) {
            if (region.status() == RegionStatus.DEFAULT || region.status() == RegionStatus.PUBLIC || region.status() == RegionStatus.INACTIVE) {

                setItem(13, Utils.createItem(Material.OAK_FENCE_GATE, 1,
                                Utils.title("Make region open"),
                                Utils.line("An open region allows all"),
                                Utils.line("Jr.Builder+ to build without"),
                                Utils.line("needing to join the region."),
                                Utils.line("Any existing members will be kicked.")),

                        u -> {
                            region.setOpen();
                            this.refresh();
                        });

            } else if (region.status() == RegionStatus.OPEN) {

                setItem(13, Utils.createItem(Material.OAK_FENCE, 1,
                                Utils.title("Make region closed"),
                                Utils.line("The default region setting,"),
                                Utils.line("people will again be required"),
                                Utils.line("to join the region to build.")),

                        u -> {
                            region.setDefault("jrbuilder");
                            this.refresh();
                        });

            }
        }

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
}
