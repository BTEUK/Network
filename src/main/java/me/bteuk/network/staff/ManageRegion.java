package me.bteuk.network.staff;

import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public class ManageRegion extends Gui {

    private final Region region;

    public ManageRegion(Region region) {

        super(27, Component.text("Manage Region", NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = region;

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
        if (region.isDefault() || region.isInactive()) {

        } else if (region.isPublic()) {

        }

        //Transfer ownership if status is default or public, must have at least 1 member.
        if (region.hasMember()) {

        }

        //Kick members, must have owner and/or members.
        if (region.hasOwner() || region.hasMember()) {

        }

        //Set region locked if region is default, public, open or inactive.
        //Set region unlocked if region is locked.
        if (region.isDefault() || region.isPublic() || region.isOpen() || region.isInactive()) {

        } else if (region.isLocked()) {

        }

        //Set region open if status is default, public or inactive.
        //Set region default if status is open.
        if (region.isDefault() || region.isPublic() || region.isInactive()) {

        } else if (region.isOpen()) {

        }

        setItem(26, Utils.createItem(Material.SPRUCE_DOOR, 1,
                        Utils.chat("&b&lPrevious Page"),
                        Utils.chat("&fOpen the staff menu.")),
                u ->

                {

                    //Return to request menu.
                    this.delete();
                    u.staffUser.manageRegion = null;

                    u.staffUser.staffGui = new StaffGui(u);
                    u.staffUser.staffGui.open(u);

                });
    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
