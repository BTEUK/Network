package me.bteuk.network.staff;

import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
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
                Utils.chat("&b&lRegion " + region.regionName()),
                Utils.chat("&fCurrent owner: &7" + region.ownerName()),
                Utils.chat("&fNumber of members: &7" + region.memberCount()),
                Utils.chat("&fRegion status: &7" + region.getStatus())));


        //Set public if status is default or inactive.
        //Set private if status is public.
        if (user.player.hasPermission("uknet.regions.manage.public")) {
            if (region.isDefault() || region.isInactive()) {

                setItem(11, Utils.createItem(Material.OAK_DOOR, 1,
                                Utils.chat("&b&lMake region public"),
                                Utils.chat("&fA public region allows members"),
                                Utils.chat("&fto join without needing"),
                                Utils.chat("&fthe owner to accept it.")),

                        u -> {

                            region.setPublic();
                            this.refresh();

                        });

            } else if (region.isPublic()) {

                setItem(11, Utils.createItem(Material.IRON_DOOR, 1,
                                Utils.chat("&b&lMake region private"),
                                Utils.chat("&fThe default region setting,"),
                                Utils.chat("&fjoining requires the owner"),
                                Utils.chat("&fto accept the request.")),

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
                                Utils.chat("&b&lTransfer Ownership"),
                                Utils.chat("&fOpen the transfer ownership menu."),
                                Utils.chat("&fAllows you to make a member"),
                                Utils.chat("&fthe new region owner.")),

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
                                Utils.chat("&b&lKick Members"),
                                Utils.chat("&fRemove any current members."),
                                Utils.chat("&for the owner from the region.")),

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
            if (region.isDefault() || region.isPublic() || region.isOpen() || region.isInactive()) {

                setItem(12, Utils.createItem(Material.IRON_TRAPDOOR, 1,
                                Utils.chat("&b&lLock Region"),
                                Utils.chat("&fLocking a region stops anyone"),
                                Utils.chat("&ffrom joining or building in the"),
                                Utils.chat("&fregion, any existing members"),
                                Utils.chat("&fwill be kicked")),

                        u -> {

                            region.setLocked();
                            this.refresh();

                        });

            } else if (region.isLocked()) {

                setItem(12, Utils.createItem(Material.OAK_TRAPDOOR, 1,
                                Utils.chat("&b&lUnlock Region"),
                                Utils.chat("&fThe default region setting,"),
                                Utils.chat("&fpeople will be able to join"),
                                Utils.chat("&fand building in the region again.")),

                        u -> {

                            region.setDefault();
                            this.refresh();

                        });
            }
        }

        //Set region open if status is default, public or inactive.
        //Set region default if status is open.
        if (user.player.hasPermission("uknet.regions.manage.open")) {
            if (region.isDefault() || region.isPublic() || region.isInactive()) {

                setItem(13, Utils.createItem(Material.OAK_FENCE_GATE, 1,
                                Utils.chat("&b&lMake region open"),
                                Utils.chat("&fAn open region allows all"),
                                Utils.chat("&fJr.Builder+ to build without"),
                                Utils.chat("&fneeding to join the region."),
                                Utils.chat("&fAny existing members will be kicked.")),

                        u -> region.setOpen());

            } else if (region.isOpen()) {

                setItem(13, Utils.createItem(Material.OAK_FENCE, 1,
                                Utils.chat("&b&lMake region closed"),
                                Utils.chat("&fThe default region setting,"),
                                Utils.chat("&fpeople will again be required"),
                                Utils.chat("&fto join the region to build.")),

                        u -> region.setDefault());

            }
        }

        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lPrevious Page"),
                        Utils.chat("&fOpen the staff menu.")),
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
