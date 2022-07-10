package me.bteuk.network.gui.regions;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.gui.Gui;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;

public class RegionInfo extends Gui {

    private final Region region;
    private final String uuid;

    private final GlobalSQL globalSQL;

    public RegionInfo(String region, String uuid) {

        super(27, Component.text("Region " + region, NamedTextColor.AQUA, TextDecoration.BOLD));

        this.region = Network.getInstance().getRegionManager().getRegion(region);
        this.uuid = uuid;

        globalSQL = Network.getInstance().globalSQL;

        createGui();

    }

    private void createGui() {

        //For owners:
        //Invite member. 9
        //Manage members. 18
        //Set public. 0

        //For both:
        //Leave region. 8
        //Set region name. 21

        //Region info.
        setItem(4, Utils.createItem(Material.BOOK, 1,
                Utils.chat("&b&lRegion " + region.getTag(uuid)),
                Utils.chat("&fRegion Owner &7" + region.ownerName()),
                Utils.chat("&fRegion Members &7" + region.memberCount())));

        //Teleport to region.
        setItem(22, Utils.createItem(Material.ENDER_PEARL, 1,
                        Utils.chat("&b&lTeleport to Region."),
                        Utils.chat("&fTeleports you to the region at the"),
                        Utils.chat("&fcurrent set location."),
                        Utils.chat("&fYou can edit the location by clicking on the"),
                        Utils.chat("&f'Set Location' button while standing in the region.")),
                u -> {

                    //If the player is on the earth server get the coordinate.
                    if (Network.SERVER_NAME.equals(globalSQL.getString("SELECT name FROM server_data WHERE type='PLOT';"))) {

                        Location l = globalSQL.getCoordinate(region.getCoordinateID(uuid));
                        u.player.teleport(l);
                        u.player.sendMessage(Utils.chat("&aTeleported to region &3" + region.getTag(uuid)));

                    } else {

                        //Create teleport region event.
                        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('" + u.player.getUniqueId() + "','network'," + "'teleport region "
                                + region + "');");

                        //Switch server.
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(globalSQL.getString("SELECT name FROM server_data WHERE type='PLOT'"));

                    }
                });

        //Set teleport location.
        setItem(23, Utils.createItem(Material.ENDER_EYE, 1,
                        Utils.chat("&b&lSet Location."),
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


    }

    public void refresh() {

        this.clearGui();
        createGui();

    }
}
