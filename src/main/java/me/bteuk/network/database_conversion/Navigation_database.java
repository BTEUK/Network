package me.bteuk.network.database_conversion;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.utils.enums.Categories;
import me.bteuk.network.utils.enums.Counties;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Navigation_database {

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

    public void navigation() {

        //Get all existing locations and add them to the list.
        ArrayList<Location> locations = new ArrayList<>();

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM location_data;");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                locations.add(new Location(results.getString("location"), results.getString("category"), results.getString("subcategory"),
                        results.getDouble("x"), results.getDouble("y"), results.getDouble("z"),
                        results.getFloat("pitch"), results.getFloat("yaw")));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Add the locations to the new database.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //Get the name of the Earth world.
        //Get the earth server name.
        String earth = Network.getInstance().getConfig().getString("earth_world");
        String server = globalSQL.getString("SELECT name FROM server_data WHERE type='EARTH';");

        Categories category;
        Counties county = null;
        int coordinate_id;

        for (Location l : locations) {

            //Get new category.
            if (l.category.equalsIgnoreCase("London")) {
                category = Categories.ENGLAND;
            } else {
                //Get the category from the existing category, converted to uppercase.
                category = Categories.valueOf(l.category.toUpperCase());
            }

            //If category is England, get the county.
            //Unless the category was London, then get either Greater London or City of London.
            if (category == Categories.ENGLAND) {
                if (l.category.equalsIgnoreCase("London")) {
                    if (l.subcategory.equalsIgnoreCase("City_of_London")) {
                        county = Counties.CITY_OF_LONDON;
                    } else {
                        county = Counties.GREATER_LONDON;
                    }
                } else {
                    county = Counties.valueOf(l.subcategory.toUpperCase());
                }
            }

            //Create coordinate_id.
            coordinate_id = globalSQL.addCoordinate(server, earth, l.x, l.y, l.z, l.yaw, l.pitch);

            //Add location to new database.
            if (category == Categories.ENGLAND) {
                globalSQL.update("INSERT INTO location_requests(location,category,subcategory,coordinate) " +
                        "VALUES('" + l.name + "','" + category + "','" + county.region + "," + coordinate_id + ";");
            } else {
                globalSQL.update("INSERT INTO location_data(location,category,coordinate) " +
                        "VALUES('" + l.name + "','" + category + "','" + coordinate_id + ";");

            }
        }


    }

}

class Location {

    String name;
    String category;
    String subcategory;
    double x;
    double y;
    double z;
    float pitch;
    float yaw;

    public Location(String name, String category, String subcategory, double x, double y, double z, float pitch, float yaw) {
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

}
