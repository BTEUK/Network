package me.bteuk.network.utils.progression;

import me.bteuk.network.Network;

import static me.bteuk.network.utils.Constants.ANNOUNCE_OVERALL_LEVELUPS;
import static me.bteuk.network.utils.Constants.ANNOUNCE_SEASONAL_LEVELUPS;
import static me.bteuk.network.utils.Constants.PROGRESSION;

public class Progression {

    /**
     * Add exp for the player to all active seasons. If the player levels up, this will be processed and announced.
     * @param uuid the uuid of the player
     * @param exp the amount of exp to add
     */
    public static void addExp(String uuid, int exp) {

        //If progression is disabled, cancel.
        if (!PROGRESSION) {
            return;
        }

        //Add exp for the overall progression.
        addExp("default", uuid, exp, ANNOUNCE_OVERALL_LEVELUPS);

        //Add exp for the active season, if it exists. There can only be 1 active season at a time.
        if (Network.getInstance().globalSQL.hasRow("SELECT id FROM seasons WHERE active=1")) {
            addExp(Network.getInstance().globalSQL.getString("SELECT id FROM seasons WHERE active=1"), uuid, exp, ANNOUNCE_SEASONAL_LEVELUPS);
        }

    }

    /**
     * Add exp for the player to a specific season. If the player levels up, this will be processed and announced.
     * @param season the season to add the exp to.
     * @param uuid the uuid of the player
     * @param exp the amount of exp to add
     * @param announce_levelups whether level-ups should be announced
     */
    private static void addExp(String season, String uuid, int exp, boolean announce_levelups) {

        int currentExp = exp + Level.getExp(season, uuid);
        int currentLevel = Level.getLevel(season, uuid);

        if (Level.reachedNextLevel(currentLevel, currentExp)) {

            //Increase level.
            currentLevel++;
            Level.setLevel(season, uuid, currentLevel);

            //Get remaing exp and set that.
            Level.setExp(season, uuid, Level.getLeftoverExp(currentLevel, currentExp));

            //Announce the levelup if enabled.
            if (announce_levelups) {

                //If the player is not online send them a message for when they next log in.


            } else {

                //Send a message to the player.

            }

        }

    }
}
