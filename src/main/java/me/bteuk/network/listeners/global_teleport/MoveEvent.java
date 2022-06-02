package me.bteuk.network.listeners.global_teleport;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.network.sql.NavigationSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.network.sql.RegionSQL;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class MoveEvent implements Listener {

    private final double yMax;
    private final double yMin;

    private final boolean enabled;
    private final String world;

    private final RegionSQL regionSQL;

    public MoveEvent(Network instance, RegionSQL regionSQL) {

        Bukkit.getServer().getPluginManager().registerEvents(this, instance);

        FileConfiguration config = instance.getConfig();
        yMax = config.getDouble("max_y");
        yMin = config.getDouble("min_y");

        enabled = config.getBoolean("earth_server");
        world = config.getString("earth_world");

        this.regionSQL = regionSQL;

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {

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

        //If this is the earth server check region enter/exit in earth world.
        if (enabled) {

            if (l.getWorld().getName().equals(world)) {

                //If the region is not the same as the existing region.
                if (!u.region.equals(l)) {

                    //Check if the player cannot enter this region cancel the action.
                    if (regionSQL.hasRow("SELECT region FROM regions WHERE region=" + u.region.getRegion(l) + ";") && !p.hasPermission("group.jrbuilder")) {

                        p.sendMessage(Utils.chat("&cThis region has not been loaded, you must be Jr.Builder+ to load new areas."));
                        e.setCancelled(true);
                        return;

                    }

                    //Check if the region is on the plotserver, teleport them there.
                    if (regionSQL.hasRow("SELECT region FROM regions WHERE region=" + u.region.getRegion(l) + " AND status='plot';")) {

                        //Get server of region.
                        String server = Network.getInstance().plotSQL.getString("SELECT server FROM regions WHERE region=" + u.region.getRegion(l) + ";");
                        String location = Network.getInstance().plotSQL.getString("SELECT location FROM regions WHERE region=" + u.region.getRegion(l) + ";");

                        int xTransform = Network.getInstance().plotSQL.getInt("SELECT xTransform FROM location_data WHERE location=" + location + ";");
                        int zTransform = Network.getInstance().plotSQL.getInt("SELECT zTransform FROM location_data WHERE location=" + location + ";");

                        //Set join event to teleport there.
                        Network.getInstance().globalSQL.update("INSERT INTO join_events(uuid,event) VALUES(" + p.getUniqueId() + "," + "teleport "
                                + location + " " + (l.getX() + xTransform) + " " + (l.getZ() + zTransform) + " " + l.getYaw() + " " + l.getPitch());

                        //Switch server.
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(server);
                        e.setCancelled(true);
                        return;

                    }

                    //Player is allowed to enter, update the region.
                    //TODO: Tell the player whether they can build in this region.
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Utils.chat("&aYou have entered " + u.region.getRegion(l) + " and left " + u.region.getRegion())));
                    u.region.setRegion(l);

                }

            } else if (u.region.inRegion) {
                u.region.inRegion = false;
            }

        }
    }
}
