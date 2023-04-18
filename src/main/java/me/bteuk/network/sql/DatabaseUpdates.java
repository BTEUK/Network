package me.bteuk.network.sql;

import me.bteuk.network.Network;

import static me.bteuk.network.utils.Constants.LOGGER;

public class DatabaseUpdates {

    //Update database if the config was outdated, this implies the database is also outdated.
    public void updateDatabase() {

        //Get the database version from the database.
        String version = "1.0.0";
        if (Network.getInstance().globalSQL.hasRow("SELECT data_value FROM unique_data WHERE data_key='version';")) {
            version = Network.getInstance().globalSQL.getString("SELECT data_value FROM unique_data WHERE data_key='version';");
        } else {
            //Insert the latest database version as version.
            Network.getInstance().globalSQL.update("INSERT INTO unique_data(data_value,data_key) VALUES('version','1.1.0'");
        }

        //Check for specific table columns that could be missing,
        //All changes have to be tested from 1.0.0.
        //We update 1 version at a time.

        //Convert config version to integer, so we can easily use them.
        int oldVersionInt = getVersionInt(version);

        //Update sequentially.

        //1.0.0 -> 1.1.0
        if (oldVersionInt <= 1) {
            update1_2();
        }
    }

    private int getVersionInt(String version) {

        switch(version) {

            //1.1.0 = 2
            case "1.1.0" -> {
                return 2;
            }

            //Default is 1.0.0 = 1;
            default -> {
                return 1;
            }

        }

    }

    private void update1_2() {

        LOGGER.info("Updating database from 1.0.0 to 1.1.0");

        //Version 1.1.0.
        //Add skin texture id column.
        Network.getInstance().globalSQL.update("ALTER TABLE player_data ADD COLUMN player_skin TEXT NULL DEFAULT NULL;");

        //Add foreign constraints.

        //id to player_data (coordinate), location_data (coordinate), location_requests (coordinate) and home (coordinate_id)
        // since it references an id from the coordinates table.
        Network.getInstance().globalSQL.update("ALTER TABLE player_data ADD FOREIGN KEY (previous_coordinate) REFERENCES coordinates(id);");
        Network.getInstance().globalSQL.update("ALTER TABLE location_data ADD FOREIGN KEY (coordinate) REFERENCES coordinates(id);");
        Network.getInstance().globalSQL.update("ALTER TABLE location_requests ADD FOREIGN KEY (coordinate) REFERENCES coordinates(id);");
        Network.getInstance().globalSQL.update("ALTER TABLE home ADD FOREIGN KEY (coordinate_id) REFERENCES coordinates(id);");

        //uuid to join_events, server_events, statistics, online_users, server_switch, moderation, coins, discord and home
        // since it references a player that will always be in the player_data table.
        Network.getInstance().globalSQL.update("ALTER TABLE join_events ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE server_events ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE statistics ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE online_users ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE join_events ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE moderation ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE coins ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE discord ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().globalSQL.update("ALTER TABLE home ADD FOREIGN KEY (uuid) REFERENCES player_data(uuid);");

        //name to online_users (server), server_switch (from_server and to_server), coordinates (server)
        // since it references servers in the server_data table.
        Network.getInstance().globalSQL.update("ALTER TABLE online_users ADD FOREIGN KEY (server) REFERENCES server_data(name);");
        Network.getInstance().globalSQL.update("ALTER TABLE server_switch ADD FOREIGN KEY (from_server) REFERENCES server_data(name);");
        Network.getInstance().globalSQL.update("ALTER TABLE server_switch ADD FOREIGN KEY (to_server) REFERENCES server_data(name);");
        Network.getInstance().globalSQL.update("ALTER TABLE coordinates ADD FOREIGN KEY (server) REFERENCES server_data(name);");


    }
}
