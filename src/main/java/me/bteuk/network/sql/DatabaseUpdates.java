package me.bteuk.network.sql;

import me.bteuk.network.Network;

import static me.bteuk.network.utils.Constants.LOGGER;

public class DatabaseUpdates {

    //Update database if the config was outdated, this implies the database is also outdated.
    public void updateDatabase() {

        //Get the database version from the database.
        String version = "1.0.0";
        if (Network.getInstance().getGlobalSQL().hasRow("SELECT data_value FROM unique_data WHERE data_key='version';")) {
            version = Network.getInstance().getGlobalSQL().getString("SELECT data_value FROM unique_data WHERE data_key='version';");
        } else {
            //Insert the current database version as version.
            Network.getInstance().getGlobalSQL().update("INSERT INTO unique_data(data_key, data_value) VALUES('version','1.4.4')");
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

        //1.1.0 -> 1.2.0
        if (oldVersionInt <= 2) {
            update2_3();
        }

        //1.2.0 -> 1.3.0
        if (oldVersionInt <= 3) {
            update3_4();
        }
    }

    private int getVersionInt(String version) {

        switch(version) {

            // 1.4.4 = 5
            case "1.4.4" ->  {
                return 5;
            }

            // 1.3.0 = 4
            case "1.3.0" -> {
                return 4;
            }

            // 1.2.0 = 3
            case "1.2.0" -> {
                return 3;
            }

            // 1.1.0 = 2
            case "1.1.0" -> {
                return 2;
            }

            // Default is 1.0.0 = 1;
            default -> {
                return 1;
            }

        }

    }

    private void update4_5() {

        LOGGER.info("Updating database from 1.3.0 to 1.4.4");

        // Version 1.4.4
        Network.getInstance().getGlobalSQL().update("UPDATE unique_data SET data_value='1.4.4' WHERE data_key='version';");

        // Update column in location_data for the new subcategory id as int.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE location_data MODIFY subcategory INT NULL DEFAULT NULL;");

        // Add foreign key to location_data referencing the new location_category table.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE home ADD fk_location_data_2 FOREIGN KEY (subcategory) REFERENCES location_category(id);");

        // Add foreign key to location_requests referencing the new location_category table.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE home ADD fk_location_requests_2 FOREIGN KEY (subcategory) REFERENCES location_category(id);");

    }

    private void update3_4() {

        LOGGER.info("Updating database from 1.2.0 to 1.3.0");

        //Version 1.3.0.
        Network.getInstance().getGlobalSQL().update("UPDATE unique_data SET data_value='1.3.0' WHERE data_key='version';");

        //Add tips_enabled to the player_data table.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE player_data ADD COLUMN tips_enabled TINYINT(1) NOT NULL DEFAULT 1;");

    }

    private void update2_3() {

        LOGGER.info("Updating database from 1.1.0 to 1.2.0");

        //Version 1.2.0.
        Network.getInstance().getGlobalSQL().update("UPDATE unique_data SET data_value='1.2.0' WHERE data_key='version';");

        //Add applicant to list of builder roles.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE player_data MODIFY builder_role ENUM('default','applicant','apprentice','jrbuilder','builder','architect','reviewer') DEFAULT 'default'");

    }

    private void update1_2() {

        LOGGER.info("Updating database from 1.0.0 to 1.1.0");

        //Version 1.1.0.
        Network.getInstance().getGlobalSQL().getString("UPDATE unique_data SET data_value='1.1.0' WHERE data_key='version';");

        //Add skin texture id column.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE player_data ADD COLUMN player_skin TEXT NULL DEFAULT NULL;");

        //Add foreign constraints.

        //id to location_data (coordinate), location_requests (coordinate) and home (coordinate_id)
        // since it references an id from the coordinates table.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE location_data ADD CONSTRAINT fk_location_data_1 FOREIGN KEY (coordinate) REFERENCES coordinates(id);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE location_requests ADD CONSTRAINT fk_location_requests_1 FOREIGN KEY (coordinate) REFERENCES coordinates(id);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE home ADD fk_home_1 FOREIGN KEY (coordinate_id) REFERENCES coordinates(id);");

        //uuid to join_events, server_events, statistics, online_users, server_switch, moderation, coins, discord and home
        // since it references a player that will always be in the player_data table.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE messages ADD fk_messages_1 FOREIGN KEY (recipient) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE join_events ADD fk_join_events_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE server_events ADD fk_server_events_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE statistics ADD fk_statistics_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE online_users ADD fk_online_users_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE server_switch ADD fk_server_switch_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE moderation ADD fk_moderation_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE coins ADD fk_coins_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE discord ADD fk_discord_1 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE home ADD fk_home_2 FOREIGN KEY (uuid) REFERENCES player_data(uuid);");

        //name to online_users (server), server_switch (from_server and to_server), coordinates (server)
        // since it references servers in the server_data table.
        Network.getInstance().getGlobalSQL().update("ALTER TABLE online_users ADD fk_online_users_2 FOREIGN KEY (server) REFERENCES server_data(name);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE server_switch ADD fk_server_switch_2 FOREIGN KEY (from_server) REFERENCES server_data(name);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE server_switch ADD fk_server_switch_3 FOREIGN KEY (to_server) REFERENCES server_data(name);");
        Network.getInstance().getGlobalSQL().update("ALTER TABLE coordinates ADD fk_coordinates_1 FOREIGN KEY (server) REFERENCES server_data(name);");

    }
}
