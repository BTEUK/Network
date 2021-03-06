package me.bteuk.network.database_conversion;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UKnet_database {

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

    public void player_data() {

        //Get all existing players and add them to Player array.
        ArrayList<Player> players = new ArrayList<>();

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT uuid,name,role,last_join FROM players;");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                players.add(new Player(results.getString(1), results.getString(2), results.getString(3), results.getLong(4)));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Add the players to the new database.
        GlobalSQL globalSQL = Network.getInstance().globalSQL;

        //Iterate through all players.
        //If the user is in the new role list then keep their role.
        //Else downgrade it by 1.
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command;

        for (Player p : players) {

            try (Connection conn = conn();
                 PreparedStatement statement = conn.prepareStatement("SELECT uuid FROM migrate_list WHERE uuid='" + p.uuid + "';");
                 ResultSet results = statement.executeQuery()) {

                //Player opted into the role migration.
                if (results.next()) {

                    command = "lp user " + p.name + " parent add " + p.role;
                    Bukkit.getServer().dispatchCommand(console, command);

                    globalSQL.update("INSERT INTO player_data(uuid,name,last_online,last_submit) VALUES('" +
                            p.uuid + "','" + p.name + "'," + p.last_join + "," + 0 + ");");

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


    }

}

class Player {

    String uuid;
    String name;
    String role;
    long last_join;

    public Player(String uuid, String name, String role, long last_join) {

        this.uuid = uuid;
        this.name = name;
        this.role = role;
        this.last_join = last_join;

    }
}
