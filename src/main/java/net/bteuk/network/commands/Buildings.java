package net.bteuk.network.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.bteuk.network.Network;
import net.bteuk.network.building_counter.Building;
import net.bteuk.network.building_counter.ConfirmationListener;
import net.bteuk.network.commands.tabcompleters.FixedArgSelector;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.enums.ServerType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import static net.bteuk.network.utils.Constants.SERVER_TYPE;

public class Buildings extends AbstractCommand {
    private static final Component ERROR = ChatUtils.error("/building add/show/count/delete/definition");

    private final Network instance;
    private Location playerloc;
    public Player player;
    private ConfirmationListener response = new ConfirmationListener(this);

    public Buildings(Network instance) {
        super();
        this.instance = instance;
        setTabCompleter(new FixedArgSelector(Arrays.asList("add", "show", "count", "delete", "definition"), 0));
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        player = getPlayer(stack);
        if (player == null) {
            return;
        }
        System.out.println(args.length);
        if (args.length < 1) {
            player.sendMessage(ERROR);
            return;
        }

        if (SERVER_TYPE != ServerType.EARTH) {
            player.sendMessage(ChatUtils.error("Please join the earth server before running this command"));
            return;
        }
        if (args[0].equals("add")) {
            if (player.hasPermission("network.buildings.add")) {

                AddBuilding(player);
            } else {
                player.sendMessage(ChatUtils.error("You need to be at least jr builder to run this command"));
            }
        } else if (args[0].equals("show")) {
            ShowBuildings(player);

        } else if (args[0].equals("count")) {
            DisplayCount(player);
        } else if (args[0].equals("delete")) {
            DeleteBuilding(player);
        } else if (args[0].equals("definition")) {
            player.sendMessage(ChatUtils.success("A building is a structure that has walls on all sides, a roof, is larger than 2*3m and can be entered by a human. In other words use common sense. It is up to you whether you count terraced houses as one or many buildings."));
        }

    }

    public void DeleteBuilding(Player player) {
        ArrayList<Building> nearbyBuildings = getNearbyBuildings(player, 5);
        double minDist = 100;
        Building minbuilding = null;
        for (Building i : nearbyBuildings) {
            double currentDist = GetDist(i.coordinate, player.getLocation());
            if (currentDist < minDist) {
                minDist = currentDist;
                minbuilding = i;
            }
        }
        if (minbuilding == null) {
            player.sendMessage(ChatUtils.error("No buildings within 5 blocks"));
            return;
        }
        if (minbuilding.playerId.equals(player.getUniqueId().toString()) || player.hasPermission("network.buildings.delete")) {
            Network.getInstance().getGlobalSQL().deleteBuilding(minbuilding);
            player.sendMessage(ChatUtils.success("Building deleted"));
        } else {
            player.sendMessage(ChatUtils.error("You don't have permission to delete this building"));

        }


    }

    public double GetDist(Location l1, Location l2) {
        double deltax = l1.getX() - l2.getX();
        double deltaz = l2.getZ() - l2.getZ();
        return Math.sqrt((deltax * deltax) + (deltaz * deltaz));
    }

    public void DisplayCount(Player player) {
        int buildingCount = Network.getInstance().getGlobalSQL().getInt("SELECT COUNT(*) FROM buildings;");
        player.sendMessage(ChatUtils.success(buildingCount + " buildings have been built!!"));

    }

    public void AddBuilding(Player player) {
        playerloc = player.getLocation();
        ArrayList<Building> nearbyBuildings = getNearbyBuildings(player, 20);
        StringBuilder locs = new StringBuilder("buildings nearby:");
        for (Building j : nearbyBuildings) {
            Location i = j.coordinate;
            locs.append(" (").append(Math.round(i.getX())).append(",").append(Math.round(i.getZ())).append("),");
        }
        if (!nearbyBuildings.isEmpty()) {
            locs.deleteCharAt(locs.length() - 1);
            player.sendMessage(ChatUtils.error("Other buildings nearby, to confirm a new building being added type 'y'. If unsure type 'n' and run /building show."));
            Bukkit.getServer().getPluginManager().registerEvents(response, Network.getInstance());
            return;
        }
        addBuildingToDataBase(player);

    }

    public void addBuildingToDataBase(Player player) {
        Location l = playerloc;
        player.sendMessage(ChatUtils.success("Building added at " + l.getX() + "," + l.getZ()));
        int CID = Network.getInstance().getGlobalSQL().addCoordinate(l);
        Network.getInstance().getGlobalSQL().update("INSERT INTO buildings (coordinate_id, player_id) VALUES (" + CID + ", '" + player.getUniqueId() + "');");

    }

    public void ShowBuildings(Player player) {
        ArrayList<Building> nearbyBuildings = getNearbyBuildings(player, 100);
        StringBuilder locs = new StringBuilder("buildings nearby:");
        for (Building j : nearbyBuildings) {
            Location i = j.coordinate;
            locs.append(" (").append(Math.round(i.getX())).append(",").append(Math.round(i.getZ())).append("),");
            Location finalHeight = new Location(i.getWorld(), i.getX(), i.getWorld().getHighestBlockYAt(i) + 1, i.getZ());
            new BukkitRunnable() {
                int duration = 200;
                int tickCount = 0;

                @Override
                public void run() {
                    if (tickCount >= duration) {
                        cancel(); // Stop the task after the duration is reached
                        return;
                    }
                    player.spawnParticle(Particle.HAPPY_VILLAGER, finalHeight, 1, 0.2, 0, 0.2);
                    tickCount++;
                }
            }.runTaskTimer(instance, 0L, 1L); // Starts immediately, repeating every 1 tick (1/20th of a second)
        }
        if (!nearbyBuildings.isEmpty()) {
            locs.deleteCharAt(locs.length() - 1);
            //player.sendMessage(ChatUtils.success(locs.toString()));
        }
    }

    public ArrayList<Building> getNearbyBuildings(Player player, int radius) {
        Location Pl = player.getLocation();
        double xmax = Pl.getX() + radius;
        double xmin = Pl.getX() - radius;
        double zmax = Pl.getZ() + radius;
        double zmin = Pl.getZ() - radius;
        String condition = "WHERE coordinates.x > " + xmin + " AND coordinates.x < " + xmax + " AND coordinates.z > " + zmin + " AND coordinates.z < " + zmax;
        return Network.getInstance().getGlobalSQL().getBuildings(condition);

    }
}
