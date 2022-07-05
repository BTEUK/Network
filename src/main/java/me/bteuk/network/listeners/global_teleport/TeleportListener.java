package me.bteuk.network.listeners.global_teleport;

import me.bteuk.network.Network;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.network.utils.enums.ServerType;
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
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    private final double yMax;
    private final double yMin;

    private final boolean regionsEnabled;
    private final String earthWorld;

    private final RegionManager regionManager;

    private final RegionSQL regionSQL;

    public TeleportListener(Network instance, RegionSQL regionSQL) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        FileConfiguration config = instance.getConfig();
        yMax = config.getDouble("max_y");
        yMin = config.getDouble("min_y");

        regionsEnabled = config.getBoolean("regions_enabled");
        earthWorld = config.getString("earth_world");

        this.regionSQL = regionSQL;

        regionManager = instance.getRegionManager();

    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        Player p = e.getPlayer();
        NetworkUser u = Network.getInstance().getUser(p);
        Location l = e.getTo();

        if (!(p.hasPermission("uknet.network.elevation.bypass"))) {

            if (l.getY() > yMax) {
                e.setCancelled(true);
                p.sendMessage(Utils.chat("&cYou may not go above y " + yMax + ", please contact staff if you need to bypass it."));
                return;
            }

            if (l.getY() < yMin) {
                e.setCancelled(true);
                p.sendMessage(Utils.chat("&cYou may not go below y " + yMin + ", please contact staff if you need to bypass it."));
                return;
            }
        }

        //If regions are enabled, check for movement between regions.
        if (regionsEnabled) {

            //Check whether the player is teleporting to a server and world that uses regions.
            //If the player is on the earth server check if they are teleporting to the earth world.
            if (Network.SERVER_NAME.equals(Network.getInstance().globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';"))) {
                if (l.getWorld().getName().equals(earthWorld)) {

                    //Get region.
                    Region region = regionManager.getRegion(l);

                    //Player is teleport to earth world on earth server.
                    //Check if are teleporting to a new region, if currently in one.
                    if (u.inRegion) {

                        //If the regions are not equal
                        if (!u.region.equals(region)) {

                            //Check if the player can enter the region.
                            if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                                //If the player is the region owner update last enter and tell set the message.
                                if (region.isOwner(p.getUniqueId().toString())) {

                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + " and left " + u.region.getName() + ", you are the owner of this region.")));
                                    regionSQL.update("UPDATE region_members SET last_enter=" + Time.currentTime() + " WHERE region='" + region.getName() + "' AND uuid='" + p.getUniqueId() + "';");

                                    //Check if the player is a region members.
                                } else if (region.isMember(p.getUniqueId().toString())) {

                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + " and left " + u.region.getName() + ", you are a member of this region.")));
                                    regionSQL.update("UPDATE region_members SET last_enter=" + Time.currentTime() + " WHERE region='" + region.getName() + "' AND uuid='" + p.getUniqueId() + "';");

                                    //Check if the region is open and the player is at least jr.builder.
                                } else if (region.isOpen() && p.hasPermission("group.jrbuilder")) {

                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + " and left " + u.region.getName() + ", you can build in this region.")));

                                } else {

                                    //Send default enter message.
                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + " and left " + u.region.getName() + ".")));

                                }

                                //Add region to database if not exists.
                                region.addToDatabase();

                                //Update the region the player is in.
                                u.region = region;

                            } else {

                                //You can't enter this region.
                                p.sendMessage(Utils.chat("&cThe terrain for this region has not been generated, you must be Jr.Builder or higher to load new terrain."));
                                e.setCancelled(true);
                            }

                        }
                    } else {

                        //Check if the player can enter the region.
                        if (region.inDatabase() || p.hasPermission("group.jrbuilder")) {

                            //If the player is the region owner update last enter and tell set the message.
                            if (region.isOwner(p.getUniqueId().toString())) {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + ", you are the owner of this region.")));
                                regionSQL.update("UPDATE region_members SET last_enter=" + Time.currentTime() + " WHERE region='" + region.getName() + "' AND uuid='" + p.getUniqueId() + "';");

                                //Check if the player is a region members.
                            } else if (region.isMember(p.getUniqueId().toString())) {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + ", you are a member of this region.")));
                                regionSQL.update("UPDATE region_members SET last_enter=" + Time.currentTime() + " WHERE region='" + region.getName() + "' AND uuid='" + p.getUniqueId() + "';");

                                //Check if the region is open and the player is at least jr.builder.
                            } else if (region.isOpen() && p.hasPermission("group.jrbuilder")) {

                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + ", you can build in this region.")));

                            } else {

                                //Send default enter message.
                                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + ".")));

                            }

                            //Add region to database if not exists.
                            region.addToDatabase();

                            //Update the region the player is in.
                            u.region = region;

                        } else {

                            //You can't enter this region.
                            p.sendMessage(Utils.chat("&cThe terrain for this region has not been generated, you must be Jr.Builder or higher to load new terrain."));
                            e.setCancelled(true);
                        }

                    }

                }
            } else if (Network.SERVER_TYPE == ServerType.PLOT) {

                //Check if the player is teleporting to a buildable world in the plot system.
                if (Network.getInstance().plotSQL.hasRow("SELECT name FROM location_data WHERE name='" + l.getWorld().getName() + "';")) {

                    //Get negative coordinate transform of new location.
                    u.dx = -Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + l.getWorld().getName() + "';");
                    u.dz = -Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + l.getWorld().getName() + "';");

                    //Alter location to add necessary coordinate transformation.
                    l.setX(l.getX() + u.dx);
                    l.setZ(l.getZ() + u.dz);

                    //Get region.
                    Region region = regionManager.getRegion(l);

                    //Check if are teleporting to a new region, if currently in one.
                    if (u.inRegion) {

                        //If the regions are not equal
                        if (!u.region.equals(region)) {

                            //Send default enter message.
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + " and left " + u.region.getName() + ".")));

                            //Update the region the player is in.
                            u.region = region;
                        }

                    } else {

                        //Send default enter message.
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + region.getName() + ".")));


                        //Update the region the player is in.
                        u.region = region;

                    }

                }
            }
        }
    }
}
