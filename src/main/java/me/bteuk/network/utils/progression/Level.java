package me.bteuk.network.utils.progression;

import me.bteuk.network.Network;

public class Level {

    private final String season;

    public Level(String season) {

        this.season = season;

    }

    /**
     * Get the next exp threshold to reach the given level.
     *
     * @param level the level to get the threshold for
     * @return the exp required to reach the level
     */
    protected int getThreshold(int level) {

        return (int) Math.round(50 * Math.log((2d * level) / 3d) + level);

    }

    /**
     * Check whether the next level has been reached.
     * @param currentLevel the current level
     * @param exp the current exp
     * @return whether the next level has been reached
     */
    public boolean reachedNextLevel(int currentLevel, int exp) {

        return (exp >= getThreshold(currentLevel + 1));

    }

    /**
     * Get the leftover exp after reaching the next level.
     * @param level the level that has been reached
     * @param currentExp the current amount of exp
     * @return the amount of exp left after leveling up
     */
    public int getLeftoverExp(int level, int currentExp) {

        return (currentExp - getThreshold(level));

    }

    /**
     * Set the level of the player in the database.
     * @param uuid the uuid of the player
     * @param level the level to set
     * @return whether the action was successful
     */
    public boolean setLevel(String uuid, int level) {

        return Network.getInstance().globalSQL.update("UPDATE progression SET lvl=" + level + " WHERE uuid='" + uuid + "' AND season='" + season + "';");

    }

    /**
     * Set the exp of the player in the database.
     * @param uuid the uuid of the player
     * @param exp the level to set
     * @return whether the action was successful
     */
    public boolean setExp(String uuid, int exp) {

        return Network.getInstance().globalSQL.update("UPDATE progression SET exp=" + exp + " WHERE uuid='" + uuid + "' AND season='" + season + "';");

    }
}
