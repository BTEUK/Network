package me.bteuk.network.listeners.global_teleport;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private final double yMax;
    private final double yMin;

    private final boolean regionsEnabled;
    private final boolean teleportEnabled;
    private final String earthWorld;

    private final RegionManager regionManager;

    public MoveListener(Network instance) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        FileConfiguration config = instance.getConfig();
        yMax = config.getDouble("max_y");
        yMin = config.getDouble("min_y");

        regionsEnabled = config.getBoolean("regions_enabled");
        teleportEnabled = config.getBoolean("global_teleport");
        earthWorld = config.getString("earth_world");

        regionManager = instance.getRegionManager();

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

        Player p = e.getPlayer();
        NetworkUser u = Network.getInstance().getUser(p);

        //Cancel event if player is switching server.
        if (u.switching) {
            e.setCancelled(true);
            return;
        }

        if (!(p.hasPermission("uknet.network.elevation.bypass"))) {

            if (e.getTo().getY() > yMax) {
                e.setCancelled(true);
                p.sendMessage(Utils.chat("&cYou may not go above y " + yMax + ", please contact staff if you need to bypass it."));
                return;
            }

            if (e.getTo().getY() < yMin) {
                e.setCancelled(true);
                p.sendMessage(Utils.chat("&cYou may not go below y " + yMin + ", please contact staff if you need to bypass it."));
                return;
            }

        }

        //If regions are enabled, check for movement between regions.
        if (regionsEnabled) {

            //If the player is currently not in a region then that implies they are in a world without regions, so movement will not effect this.
            //Not being in a region also means that region is null.
            if (u.inRegion) {

                Location l = e.getTo().clone();

                //Alter location to add necessary coordinate transformation.
                l.setX(l.getX() + u.dx);
                l.setZ(l.getZ() + u.dz);

                //Check if the player has moved to another region.
                if (!u.region.equals(regionManager.getRegion(l))) {

                    //Get new region.
                    Region region = regionManager.getRegion(l);

                    //Check if the new region is on this server or not.
                    if (!u.region.getServer().equals(region.getServer())) {

                        //If cross-server teleport is enabled teleport them to the correct server and location.
                        if (teleportEnabled) {

                            //Check if the player can enter the region.
                            if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                                //Region is on another server, teleport them accordingly.
                                //If the new region is on a plot server, check for coordinate transform.
                                String server;
                                if (region.isPlot()) {

                                    //Get server and world of region.
                                    server = Network.getInstance().plotSQL.getString("SELECT server FROM regions WHERE region='" + region.regionName() + "';");
                                    String location = Network.getInstance().plotSQL.getString("SELECT location FROM regions WHERE region='" + region.regionName() + "';");

                                    int xTransform = Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + location + "';");
                                    int zTransform = Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + location + "';");

                                    //Set join event to teleport there.
                                    Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('" + p.getUniqueId() + "','network','teleport "
                                            + location + " " + (l.getX() + xTransform) + " " + (l.getZ() + zTransform) + " " + l.getYaw() + " " + l.getPitch() + "');");

                                } else {

                                    //Location is on the earth server.
                                    server = Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';");

                                    //Set join event to teleport there.
                                    Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,type,event) VALUES('" + p.getUniqueId() + "','network','teleport "
                                            + earthWorld + " " + l.getX() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch() + "');");

                                }

                                //Switch server.
                                u.switching = true;
                                SwitchServer.switchServer(u.player, server);

                            } else {

                                //You can't enter this region.
                                p.sendMessage(Utils.chat("&cThe terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
                            }

                        } else {

                            //Cancel movement as the location is on another server.
                            p.sendMessage(Utils.chat("&cThe terrain for this location is on another server, you may not enter."));
                        }
                        e.setCancelled(true);
                    } else {

                        //Check if the player can enter the region.
                        if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                            //If the player is the region owner update last enter and tell set the message.
                            if (region.isOwner(p.getUniqueId().toString())) {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getTag(p.getUniqueId().toString()) + " and left " + u.region.getTag(p.getUniqueId().toString()) + ", you are the owner of this region.")));
                                region.setLastEnter(p.getUniqueId().toString());

                                //Check if the player is a region members.
                            } else if (region.isMember(p.getUniqueId().toString())) {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getTag(p.getUniqueId().toString()) + " and left " + u.region.getTag(p.getUniqueId().toString()) + ", you are a member of this region.")));
                                region.setLastEnter(p.getUniqueId().toString());

                                //Check if the region is open and the player is at least jr.builder.
                            } else if (region.isOpen() && p.hasPermission("group.jrbuilder")) {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getTag(p.getUniqueId().toString()) + " and left " + u.region.getTag(p.getUniqueId().toString()) + ", you can build in this region.")));

                            } else {

                                //Send default enter message.
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getTag(p.getUniqueId().toString()) + " and left " + u.region.getTag(p.getUniqueId().toString()) + ".")));

                            }

                            //Add region to database if not exists.
                            region.addToDatabase();

                            //Update the region the player is in.
                            u.region = region;

                        } else {

                            //You can't enter this region.
                            p.sendMessage(Utils.chat("&cThe terrain for this region has not been generated, you must be at least Jr.Builder to load new terrain."));
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }

        //Network.getInstance().getLogger().info("Move: " + e.getTo().getX() + "," + e.getTo().getZ());

    }
}
