package net.bteuk.network.eventing.events;

import io.papermc.lib.PaperLib;
import net.bteuk.network.Network;
import net.bteuk.network.utils.SwitchServer;
import net.bteuk.network.utils.Utils;
import net.bteuk.network.utils.enums.ServerType;
import net.bteuk.network.utils.regions.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.UUID;

import static net.bteuk.network.utils.Constants.SERVER_TYPE;

public class TeleportEvent extends AbstractEvent {

    private final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("##.#####");

    @Override
    public void event(String uuid, String[] event, String message) {

        //Get player.
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));

        if (p == null) {
            Network.getInstance().getLogger().warning("Player is null in teleport event.");
            return;
        }

        //Check if the teleport is to a specific player.
        switch (event[1]) {
            case "player" -> {

                //Get player if they're online and teleport the player there.
                Player player = Bukkit.getPlayer(UUID.fromString(event[2]));
                if (player != null) {

                    p.teleport(player.getLocation());
                    p.sendMessage(Utils.success("Teleported to ")
                            .append(Component.text(Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + event[2] + "';"), NamedTextColor.DARK_AQUA)));

                } else {
                    p.sendMessage(Component.text(Network.getInstance().getGlobalSQL().getString("SELECT name FROM player_data WHERE uuid='" + event[2] + "';"), NamedTextColor.DARK_RED)
                            .append(Utils.error(" is not online.")));
                }
            }

            //Check if the teleport is to a specific coordinate ID.
            case "coordinateID" -> {

                p.teleport(Network.getInstance().getGlobalSQL().getLocation(Integer.parseInt(event[2])));

                //Check if a message is set.
                if (message == null) {
                    p.sendMessage(Utils.success("Teleported to previous location."));
                } else {
                    p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
                }
            }
            case "location", "location_request" -> {

                //Get location name from all remaining args.
                String location = String.join(" ", Arrays.copyOfRange(event, 2, event.length));

                //Get the coordinate id.
                int coordinate_id;
                if (event[1].equals("location")) {
                    coordinate_id = Network.getInstance().getGlobalSQL().getInt("SELECT coordinate FROM location_data WHERE location='" + location + "';");
                } else {
                    coordinate_id = Network.getInstance().getGlobalSQL().getInt("SELECT coordinate FROM location_requests WHERE location='" + location + "';");
                }

                Location l = Network.getInstance().getGlobalSQL().getLocation(coordinate_id);

                String worldName = Network.getInstance().getGlobalSQL().getString("SELECT world FROM coordinates WHERE id=" + coordinate_id + ";");

                //Check if world is in plotsystem.
                if (Network.getInstance().getPlotSQL().hasRow("SELECT name FROM location_data WHERE name='" + worldName + "';")) {

                    //Add coordinate transformation.
                    l = new Location(
                            Bukkit.getWorld(worldName),
                            l.getX() + Network.getInstance().getPlotSQL().getInt("SELECT xTransform FROM location_data WHERE name='" + worldName + "';"),
                            l.getY(),
                            l.getZ() + Network.getInstance().getPlotSQL().getInt("SELECT zTransform FROM location_data WHERE name='" + worldName + "';"),
                            l.getYaw(),
                            l.getPitch()
                    );

                }

                p.teleport(l);
                p.sendMessage(Utils.success("Teleported to ")
                        .append(Component.text(location, NamedTextColor.DARK_AQUA)));

            }
            case "region" -> {

                //Get the region.
                Region region = Network.getInstance().getRegionManager().getRegion(event[2]);
                Location l = Network.getInstance().getGlobalSQL().getLocation(region.getCoordinateID(uuid));

                if (l == null) {
                    p.sendMessage(Utils.error("An error occurred while fetching the location to teleport."));
                    Network.getInstance().getLogger().warning("Location is null for coodinate id " + region.getCoordinateID(uuid));
                    return;
                }

                //Teleport player.
                p.teleport(l);
                p.sendMessage(Utils.success("Teleported to region ")
                        .append(Component.text(region.getTag(uuid), NamedTextColor.DARK_AQUA)));

            }
            case "server" -> //Switch to server.
                    SwitchServer.switchServer(p, event[2]);

            case "spawn" -> {

                //If server is Lobby, teleport to spawn.
                if (SERVER_TYPE == ServerType.LOBBY) {
                    p.teleport(Network.getInstance().getLobby().spawn);
                    p.sendMessage(Utils.success("Teleported to spawn."));
                } else {

                    //Set teleport event to go to spawn.
                    EventManager.createTeleportEvent(true, p.getUniqueId().toString(), "network", "teleport spawn", p.getLocation());
                    SwitchServer.switchServer(p, Network.getInstance().getGlobalSQL().getString("SELECT name FROM server_data WHERE type='LOBBY';"));

                }
            }

            //Tpll command format.
            case "tpll" -> Network.getInstance().getTpll().tpll(p, Arrays.copyOfRange(event, 2, event.length), true);

            default -> {

                //Get world.
                World world = Bukkit.getWorld(event[1]);

                if (world == null) {
                    p.sendMessage(Utils.error("World can not be found."));
                    return;
                }

                double x;
                double y;
                double z;
                float yaw;
                float pitch;

                if (event.length == 6) {
                    //Length 6 means no y is specified.

                    //Get x and z.
                    x = Double.parseDouble(event[2]);
                    z = Double.parseDouble(event[3]);

                    //Get y elevation for teleport.
                    y = world.getHighestBlockYAt((int) x, (int) z);
                    y++;

                    //Get pitch and yaw.
                    yaw = Float.parseFloat(event[4]);
                    pitch = Float.parseFloat(event[5]);
                } else {
                    //Length 7 means y is specific.

                    //Get x, y and z.
                    x = Double.parseDouble(event[2]);
                    y = Double.parseDouble(event[3]);
                    z = Double.parseDouble(event[4]);

                    //Get pitch and yaw.
                    yaw = Float.parseFloat(event[5]);
                    pitch = Float.parseFloat(event[6]);
                }

                //Create location.
                Location l = new Location(world, x, y, z, yaw, pitch);


                //If the terrain has not been generated, let the player know it could take a while.
                if (!PaperLib.isChunkGenerated(l)) {
                    Utils.success("Location is generating, please wait a moment...");
                }

                //Teleport player.
                PaperLib.teleportAsync(p, l);

                //If custom message is set, send that to player, else send default message.
                if (message == null) {
                    p.sendMessage(Utils.success("Teleported to ")
                            .append(Component.text(DECIMAL_FORMATTER.format(x), NamedTextColor.DARK_AQUA))
                            .append(Utils.success(", "))
                            .append(Component.text(y, NamedTextColor.DARK_AQUA))
                            .append(Utils.success(", "))
                            .append(Component.text(DECIMAL_FORMATTER.format(z), NamedTextColor.DARK_AQUA)));
                } else {
                    p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
                }
            }
        }
    }
}
