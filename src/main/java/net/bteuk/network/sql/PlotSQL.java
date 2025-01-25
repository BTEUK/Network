package net.bteuk.network.sql;

import net.bteuk.network.lib.enums.PlotDifficulties;
import net.bteuk.network.lib.utils.Reviewing;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlotSQL extends AbstractSQL {

    public PlotSQL(BasicDataSource datasource) {
        super(datasource);
    }

    public int[][] getPlotCorners(int plotID) {

        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement("SELECT COUNT(corner) FROM plot_corners WHERE id=" + plotID + ";");
                ResultSet results = statement.executeQuery()
        ) {

            results.next();

            int[][] corners = new int[results.getInt(1)][2];

            getPlotCorners(corners, plotID);

            return corners;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int[][] getPlotCorners(int[][] corners, int plotID) {

        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement("SELECT x,z FROM plot_corners WHERE id=" + plotID + ";");
                ResultSet results = statement.executeQuery()
        ) {

            for (int i = 0; i < corners.length; i++) {

                results.next();
                corners[i][0] = results.getInt(1);
                corners[i][1] = results.getInt(2);

            }

            return corners;

        } catch (SQLException e) {
            e.printStackTrace();
            return corners;
        }
    }

    // Creates a new plot and returns the id of the plot.
    public int createPlot(int size, int difficulty, String location, int coordinate_id) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO plot_data(status, size, difficulty, location, coordinate_id) VALUES(?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        )) {

            statement.setString(1, "unclaimed");
            statement.setInt(2, size);
            statement.setInt(3, difficulty);
            statement.setString(4, location);
            statement.setInt(5, coordinate_id);
            statement.executeUpdate();

            //If the id does not exist return 0.
            try (ResultSet results = statement.getGeneratedKeys()) {
                if (results.next()) {

                    return results.getInt(1);

                } else {

                    return 0;

                }
            }

        } catch (SQLException sql) {

            sql.printStackTrace();
            return 0;

        }

    }

    // Creates a new plot and returns the id of the plot.
    public int createZone(String location, long expiration, boolean is_public) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO zones(location,expiration,is_public) VALUES(?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        )) {

            statement.setString(1, location);
            statement.setLong(2, expiration);
            statement.setBoolean(3, is_public);
            statement.executeUpdate();

            //If the id does not exist return 0.
            try (ResultSet results = statement.getGeneratedKeys()) {
                if (results.next()) {

                    return results.getInt(1);

                } else {

                    return 0;

                }
            }

        } catch (SQLException sql) {

            sql.printStackTrace();
            return 0;

        }
    }

    public Double getReviewerReputation(String uuid) {
        try (
                Connection conn = conn();
                PreparedStatement statement = conn.prepareStatement(
                        "SELECT reputation FROM reviewers WHERE uuid=?;"
                )
        ) {
            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return results.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Integer> getReviewablePlots(String uuid, boolean isArchitect, boolean isReviewer) {
        List<PlotDifficulties> difficulties = Reviewing.getAvailablePlotDifficulties(isArchitect, isReviewer, getReviewerReputation(uuid));

        List<Integer> submitted_plots = new ArrayList<>();

        for (PlotDifficulties difficulty : difficulties) {
            submitted_plots.addAll(getIntList("SELECT pd.id FROM plot_data AS pd INNER JOIN plot_submission AS ps ON pd.id=ps.id WHERE pd.status='submitted' AND pd.difficulty=" + difficulty.getValue() + " ORDER BY ps.submit_time ASC;"));
        }

        // Get all plots that the user is the owner or a member of, don't use those in the count.
        List<Integer> member_plots = getIntList("SELECT id FROM plot_members WHERE uuid='" + uuid + "';");

        submitted_plots.removeAll(member_plots);

        return submitted_plots;
    }

    public int getReviewablePlotCount(String uuid, boolean isArchitect, boolean isReviewer) {
        return getReviewablePlots(uuid, isArchitect, isReviewer).size();
    }
}
