package me.bteuk.network.server_conversion.regions;

import me.bteuk.network.Network;
import me.bteuk.network.utils.regions.Region;
import me.bteuk.network.utils.regions.RegionManager;
import net.buildtheearth.terraminusminus.dataset.IScalarDataset;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class DatabaseRegions {

    private final EarthGeneratorSettings bteGeneratorSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
    private final World world = Bukkit.getWorld(Network.getInstance().getConfig().getString("earth_world"));

    /*

    This class will convert all the regions from the old database to the new database.
    Regions will be kept the same, and if functions are different they will be represented as similar as possible.

    Regions will be added to the region manager from which they can be added to the database.
    If the region has special properties this will be done after.

     */

    //Get old database connection.
    public Connection conn() throws SQLException {

        FileConfiguration config = Network.getInstance().getConfig();

        String host = config.getString("host");
        int port = config.getInt("port");
        String username = config.getString("username");
        String password = config.getString("password");

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + config.getString("database_uknet") + "?&useSSL=false&");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource.getConnection();

    }

    public void getRegions() {

        //Get the region manager.
        RegionManager regionManager = Network.getInstance().getRegionManager();

        //Iterate through all regions and add them to the region manager.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM regions;");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                //Get the region.
                Region region = regionManager.getRegion(results.getString("region"));

                //Add it to the database.
                region.addToDatabase();

                //Check for special cases.
                if (results.getBoolean("public")) {
                    region.setPublic();
                }

                if (results.getBoolean("locked")) {
                    region.setLocked();
                }

                if (results.getBoolean("open")) {
                    region.setOpen();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void convertOwners() {

        //Get the region manager.
        RegionManager regionManager = Network.getInstance().getRegionManager();

        int xcentre, zcentre;
        double[] coords;

        //Get all the existing region members.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM region_owners;");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                //If the region is not open, add the user to the region.
                if (!regionManager.getRegion(results.getString("region")).isOpen()) {

                    //If this region already has a member, copy their coordinate id to prevent duplicate entries in the database.
                    if (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + results.getString("id") + "';")) {

                        int coordinateID = Network.getInstance().regionSQL.getInt("SELECT coordinateID FROM region_members WHERE region='" + results.getString("id") + "';");
                        //Add member.
                        addMember(results.getString("region"), results.getString("uuid"),
                                results.getBoolean("is_owner"), results.getLong("last_enter"), coordinateID);

                    } else {

                        //Create a new coordinateID at the centre of the region.

                        //Get coordinates of region centre.
                        xcentre = Integer.parseInt(results.getString("region").split(",")[0]) * 512 + 255;
                        zcentre = Integer.parseInt(results.getString("region").split(",")[1]) * 512 + 255;

                        //Convert region centre to irl coordinates.
                        coords = bteGeneratorSettings.projection().toGeo(xcentre, zcentre);

                        //Get altitude.
                        CompletableFuture<Double> altFuture;
                        try {
                            altFuture = bteGeneratorSettings.datasets()
                                    .<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_HEIGHTS)
                                    .getAsync(coords[0], coords[1])
                                    .thenApply(a -> a + 1.0d);
                        } catch (OutOfProjectionBoundsException e) {
                            altFuture = CompletableFuture.completedFuture(0d);
                        }

                        int finalXcentre = xcentre;
                        int finalZcentre = zcentre;

                        //Get info to store since the resultset will be closed before the altFuture completes.
                        String region = results.getString("region");
                        String uuid = results.getString("uuid");
                        long last_enter = results.getLong("last_enter");

                        altFuture.thenAccept(s -> Bukkit.getScheduler().runTask(Network.getInstance(), () -> {

                            int coordinateID = Network.getInstance().globalSQL.addCoordinate(new Location(world, finalXcentre, s, finalZcentre));

                            //Add member.
                            addMember(region, uuid, true, last_enter, coordinateID);
                        }));
                    }
                }

            }


        } catch (SQLException | OutOfProjectionBoundsException e) {
            e.printStackTrace();
        }
    }

    public void convertMembers() {

        //Get the region manager.
        RegionManager regionManager = Network.getInstance().getRegionManager();

        int xcentre, zcentre;
        double[] coords;

        //Get all the existing region members.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM region_members;");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                //If the region is not open, add the user to the region.
                if (!regionManager.getRegion(results.getString("region")).isOpen()) {

                    //If this region already has a member, copy their coordinate id to prevent duplicate entries in the database.
                    if (Network.getInstance().regionSQL.hasRow("SELECT region FROM region_members WHERE region='" + results.getString("id") + "';")) {

                        int coordinateID = Network.getInstance().regionSQL.getInt("SELECT coordinateID FROM region_members WHERE region='" + results.getString("id") + "';");
                        //Add member.
                        addMember(results.getString("region"), results.getString("uuid"),
                                results.getBoolean("is_owner"), results.getLong("last_enter"), coordinateID);

                    } else {

                        //Create a new coordinateID at the centre of the region.

                        //Get coordinates of region centre.
                        xcentre = Integer.parseInt(results.getString("region").split(",")[0]) * 512 + 255;
                        zcentre = Integer.parseInt(results.getString("region").split(",")[1]) * 512 + 255;

                        //Convert region centre to irl coordinates.
                        coords = bteGeneratorSettings.projection().toGeo(xcentre, zcentre);

                        //Get altitude.
                        CompletableFuture<Double> altFuture;
                        try {
                            altFuture = bteGeneratorSettings.datasets()
                                    .<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_HEIGHTS)
                                    .getAsync(coords[0], coords[1])
                                    .thenApply(a -> a + 1.0d);
                        } catch (OutOfProjectionBoundsException e) {
                            altFuture = CompletableFuture.completedFuture(0d);
                        }

                        int finalXcentre = xcentre;
                        int finalZcentre = zcentre;

                        //Get info to store since the resultset will be closed before the altFuture completes.
                        String region = results.getString("region");
                        String uuid = results.getString("uuid");
                        long last_enter = results.getLong("last_enter");

                        altFuture.thenAccept(s -> Bukkit.getScheduler().runTask(Network.getInstance(), () -> {

                            int coordinateID = Network.getInstance().globalSQL.addCoordinate(new Location(world, finalXcentre, s, finalZcentre));

                            //Add member.
                            addMember(region, uuid, false, last_enter, coordinateID);
                        }));
                    }
                }

            }


        } catch (SQLException | OutOfProjectionBoundsException e) {
            e.printStackTrace();
        }

    }

    private void addMember(String region, String uuid, boolean isOwner, long lastEnter, int coordinateID) {
        Network.getInstance().regionSQL.update("INSERT INTO region_members(region,uuid,is_owner,last_enter,coordinate_id) " +
                "VALUES('" + region + "','" + uuid + "'," + isOwner + "," + lastEnter + "," + coordinateID + ");");

    }

    public void convertLogs() {

        int is_owner;

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM logs;");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                if (results.getString("role").equalsIgnoreCase("owner")) {
                    is_owner = 1;
                } else {
                    is_owner = 0;
                }

                //Add log to new database.
                Network.getInstance().regionSQL.update("INSERT INTO region_logs(region,uuid,is_owner,start_time,end_time) VALUES('" +
                        results.getString("region") + "','" + results.getString("uuid") + "'," + is_owner + "," +
                        results.getLong("start_time") + "," + results.getLong("end_time") + ");");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
