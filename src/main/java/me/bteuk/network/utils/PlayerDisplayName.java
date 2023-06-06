package me.bteuk.network.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import static me.bteuk.network.utils.Constants.LOGGER;

public class PlayerDisplayName {

    Scoreboard scoreboard;

    public PlayerDisplayName() {

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getMainScoreboard();

        //Create a team for each role.
        for (char ch: "abcdefghijklm".toCharArray()) {

            Team team = scoreboard.getTeam(String.valueOf(ch));

            if (team != null) {

                //Remove all players from the team.
                team.removeEntries(team.getEntries());
                team.prefix(Component.text(""));

            } else {

                //Create team.
                team = scoreboard.registerNewTeam(String.valueOf(ch));

            }
        }
    }

    public void addEntry(String name, String teamName) {

        Team team = scoreboard.getTeam(teamName);

        if (team != null) {

            LOGGER.info("Added " + name + " to team " + teamName);

            team.addEntry(name);
        }
    }

    public void removeEntry(String name, String teamName) {

        Team team = scoreboard.getTeam(teamName);

        if (team != null) {
            team.removeEntry(name);
        }
    }
}
