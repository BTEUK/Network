package me.bteuk.network.utils;

import me.bteuk.network.Network;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.bteuk.network.utils.Constants.LOGGER;
import static me.bteuk.network.utils.NetworkConfig.CONFIG;

/**
 * Class that manages the automated tips in chat.
 * The frequency is specified in the config.
 * Each builder role can have a file, if no file exists for the role, no message is sent.
 */
public class Tips {

    HashMap<String, TipsList> tipsMap;

    /**
     * Load the tips from the text files in the tips folder, if any exist.
     * If no files exist in the directory don't load tips.
     */
    public Tips() {

        //Create the directory if not exists.
        File file = new File(Network.getInstance().getDataFolder() + "/tips");

        if (!file.exists()) {

            if (file.mkdir()) {

                //Add example file.
                try {

                    FileUtils.copyToFile(Objects.requireNonNull(Network.getInstance().getResource("tips-example.txt")), new File(file + "/tips-example.txt"));
                    LOGGER.info("Created tips directory and added example file.");

                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }

            }
        } else {

            //The directory exists, therefore load all txt files.
            File[] files = file.listFiles();

            if (files != null) {

                tipsMap = new HashMap<>();

                for (File txtFile : files) {

                    try {
                        List<String> lines = Files.readAllLines(Path.of(txtFile.getAbsolutePath()));

                        //Trim the list of whitespace lines.
                        lines = lines.stream().filter(str -> !str.trim().isEmpty()).collect(Collectors.toList());

                        //The file must contain at least 1 line.
                        if (!lines.isEmpty()) {
                            Collections.shuffle(lines);
                            tipsMap.put(txtFile.getName().replace(".txt", ""), new TipsList(lines));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //If the tipsMap is not empty start the tips timer.
                if (!tipsMap.isEmpty()) {

                    //Get interval.
                    long frequency = CONFIG.getInt("chat.tips.frequency") * 60L * 20L;

                    Bukkit.getScheduler().scheduleSyncRepeatingTask(Network.getInstance(), () -> {

                        //For all online players see if their builder role has tips, if true send them the current tip.
                        for (NetworkUser user : Network.getInstance().getUsers()) {

                            //Check if the user has tips enabled.
                            if (user.isTips_enabled()) {

                                //Get builder role from database.
                                String role = Network.getInstance().globalSQL.getString("SELECT builder_role FROM player_data WHERE uuid='" + user.player.getUniqueId() + "';");

                                if (tipsMap.containsKey(role)) {
                                    user.player.sendMessage(Utils.tip(tipsMap.get(role).getTip()));
                                }

                            }
                        }

                        //Increment the counter on all TipsLists
                        tipsMap.values().forEach(TipsList::increment);

                    }, 2400L, frequency);

                    LOGGER.info("Enabled tips timer!");

                }
            }
        }
    }

    /**
     * A list of tips with functionality to get the next tip.
     */
    private static class TipsList {

        private final String[] tips;
        private int counter;

        /**
         * Creates a new TipsList using a List of tips.
         *
         * @param tipsList list of tips
         */
        public TipsList(List<String> tipsList) {

            tips = tipsList.toArray(new String[0]);
            counter = 0;

        }

        /**
         * Increase the index counter, if at the maximum value set to 0.
         */
        public void increment() {

            if (counter >= (tips.length - 1)) {
                counter = 0;
            } else {
                counter++;
            }

        }

        /**
         * Get the tip at the current index.
         * @return tip at the index counter
         */
        public String getTip() {
            return tips[counter];
        }
    }
}
