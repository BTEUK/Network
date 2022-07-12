package me.bteuk.network.gui.regions;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionTagListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;

public class RegionInfo extends Gui {

    private final Region region;
    private final String uuid;

    private final GlobalSQL globalSQL;

    public RegionInfo(Region region, String uuid) {

        super(27, Component.text("Region " + region.getTag(uuid), NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = region;
        this.uuid = uuid;

        globalSQL = Network.getInstance().globalSQL;

        createGui();

    }

    private void createGui() {

        //For owners:
        //Invite member. 9
        //Manage members. 18
        //Set public. 0

        //Region info.
        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.chat("&b&lRegion " + region.getTag(uuid)),
                Utils.chat("&fRegion Owner &7" + region.ownerName()),
                Utils.chat("&fRegion Members &7" + region.memberCount())));

        //Leave Region.
        setItem(8, Utils.createItem(Material.RED_CONCRETE, 1,
                        Utils.chat("&b&lLeave Region")),
                u -> {

                    //Send leave event to server events.
                    Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','network','"
                            + globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';") + "','region leave " + region.getName() + "');");

                    //Return to region menu and close inventory.
                    this.delete();
                    u.regionInfo = null;

                    u.regionMenu = new RegionMenu(u);

                });

        //Set region tag.
        setItem(21, Utils.createItem(Material.WRITABLE_BOOK, 1,
                        Utils.chat("&b&lSet Region Tag"),
                        Utils.chat("&fClick to give this region a custom name."),
                        Utils.chat("&fYou will be prompted to type a name in chat."),
                        Utils.chat("&fIt can have a maximum of 64 characters.")),

                u -> {

                    //Create chat listener and send message telling the player.
                    //Listener will automatically close after 1 minute or if a message is sent.
                    new RegionTagListener(u.player, region);
                    u.player.sendMessage(Utils.chat("&aWrite your region tag in chat, the first message counts."));
                    u.player.closeInventory();

                });

        //Teleport to region.
        setItem(22, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lTeleport to Region"),
                        Utils.chat("&fTeleports you to the region at the"),
                        Utils.chat("&fcurrent set location."),
                        Utils.chat("&fYou can edit the location by clicking on the"),
                        Utils.chat("&f'Set Location' button while standing in the region.")),
                u -> {

                    //If the player is on the earth server get the coordinate.
                    if (Network.SERVER_NAME.equals(globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"))) {

                        //Close inventory.
                        u.player.closeInventory();

                        Location l = globalSQL.getCoordinate(region.getCoordinateID(uuid));
                        u.player.teleport(l);
                        u.player.sendMessage(Utils.chat("&aTeleported to region &3" + region.getTag(uuid)));

                    } else {

                        //Create teleport region event.
                        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('" + u.player.getUniqueId() + "','network'," + "'region teleport "
                                + region + "');");

                        //Switch server.
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(globalSQL.getString("SELECT name FROM server_data WHERE type='PLOT'"));

                    }
                });

        //Set teleport location.
        setItem(23, Utils.createItem(Material.ENDER_EYE, 1,
                        Utils.chat("&b&lSet Location"),
                        Utils.chat("&fSets the teleport location of this region"),
                        Utils.chat("&fto you current location."),
                        Utils.chat("&fYou must be standing in the region"),
                        Utils.chat("&ffor this to work.")),
                u -> {

                    //Check if the player is in the correct region.
                    if (u.inRegion) {
                        if (u.region.equals(region)) {

                            //Create coordinate id for location of player and set that as the new coordinate id.
                            region.setCoordinateID(uuid, globalSQL.addCoordinate(u.player.getLocation()));
                            u.player.sendMessage(Utils.chat("&aSet teleport location for region &3" + region.getTag(uuid) + " &aat your current location."));

                        } else {
                            u.player.sendMessage(Utils.chat("&cYou are not standing in the correct region."));
                        }
                    } else {
                        u.player.sendMessage(Utils.chat("&cYou are not standing in a region."));
                    }
                });

        //Owner only settings.
        if (region.isOwner(uuid)) {

            //If region is private, make public button, if public make private button.
            if (region.isPublic()) {
                setItem(0, Utils.createItem(Material.IRON_TRAPDOOR, 1,
                                Utils.chat("&b&lMake Private"),
                                Utils.chat("&fNew members will need your approval to join the region.")),
                        u -> {

                            //Set the region as private and refresh gui.
                            region.setPrivate();

                            u.player.sendMessage(Utils.chat("&aRegion &3" + region.getTag(uuid) + " &ais now private."));
                            this.refresh();

                        });
            } else {
                setItem(0, Utils.createItem(Material.OAK_TRAPDOOR, 1,
                                Utils.chat("&b&lMake Public"),
                                Utils.chat("&fNew members can join the region without approval.")),
                        u -> {

                            //Set the region as public and refresh gui.
                            region.setPublic();

                            //Approve any active region requests for this region.
                            //Make sure this is done on the correct server.
                            //Create teleport region event.
                            if (region.hasRequests()) {
                                Network.getInstance().globalSQL.update("INSERT INTO server_events(uuid,type,server,event) VALUES('" + u.player.getUniqueId() + "','network'," +
                                        Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';") + "'region request accept "
                                        + region + "');");
                            }

                            u.player.sendMessage(Utils.chat("&aRegion &3" + region.getTag(uuid) + " &ais now public."));
                            this.refresh();

                        });
            }

            //Invite member.
            setItem(9, Utils.createItem(Material.OAK_BOAT, 1,
                            Utils.chat("&b&lInvite Members"),
                            Utils.chat("&fInvite a new member to your region."),
                            Utils.chat("&fYou can only invite online users.")),
                    u -> {

                        //Open the invite member menu.
                        this.delete();
                        u.regionInfo = null;

                        u.inviteRegionMembers = new InviteRegionMembers();
                        u.inviteRegionMembers.open(u);

                    });

            //Manage members.
            setItem(18, Utils.createItem(Material.OAK_BOAT, 1,
                            Utils.chat("&b&lRegion Members"),
                            Utils.chat("&fManage the members in your region.")),
                    u -> {

                        //Open the invite member menu.
                        this.delete();
                        u.regionInfo = null;

                        u.regionMembers = new RegionMembers();
                        u.regionMembers.open(u);

                    });
        }

    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
