package net.bteuk.network.utils;

import net.bteuk.network.Network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a tutorial recommendation
 */
public class TutorialRecommendation
{
    private final int iRecommendationID;

    private final int iPlotID;

    private final teachingtutorials.tutorialobjects.TutorialRecommendation linkedTutorialRecommendation;

    public TutorialRecommendation(int iRecommendationID, int iPlotID)
    {
        this.iRecommendationID = iRecommendationID;
        this.iPlotID = iPlotID;
        this.linkedTutorialRecommendation = teachingtutorials.tutorialobjects.TutorialRecommendation.fetchTutorialRecommendationByID(Network.getInstance().getTutorialsDBConnection(), Constants.LOGGER, iRecommendationID);
    }

    public teachingtutorials.tutorialobjects.TutorialRecommendation getLinkedTutorialRecommendation()
    {
        return linkedTutorialRecommendation;
    }

    /**
     * Fetches a list of tutorial recommendations for a given plot
     * @param logger A logger to output to
     * @param iPlotID The ID of the plot to fetch the recommended tutorials of
     * @return A list of tutorial recommendations
     */
    public static TutorialRecommendation[] fetchTutorialRecommendationsForPlot(Logger logger, int iPlotID)
    {
        //SQL objects
        String sql;
        ResultSet resultSet;

        TutorialRecommendation[] recommendations;


        //A count of the number of tutorial recommendations for this plot
        int iCount = 0;

        try
        {
            sql = "SELECT Count(1) FROM tutorial_recommendations WHERE plot_id = "+iPlotID;
            iCount = Network.getInstance().getPlotSQL().getInt(sql);
            recommendations = new TutorialRecommendation[iCount];


            sql = "SELECT * FROM tutorial_recommendations WHERE plot_id = "+iPlotID;
            resultSet = Network.getInstance().getPlotSQL().getResultSet(sql);
            for (int i = 0 ; i < iCount ; i++)
            {
                resultSet.next();
                recommendations[i] = new TutorialRecommendation(resultSet.getInt("recommendation_id"), iPlotID);
            }
            resultSet.close();
        }
        catch (SQLException e)
        {
            logger.log(Level.WARNING, "Error fetching tutorial recommendations for plot "+iPlotID, e);
            return new TutorialRecommendation[0];
        }

        return recommendations;
    }


    /**
     * Adds this tutorials recommendation to the plots database
     */
    public void addTutorialRecommendationToDB()
    {
        String sql = "INSERT INTO tutorial_recommendations (`plot_id`, `recommendation_id`) VALUES (" +this.iPlotID+", "+this.iRecommendationID +")";
        Network.getInstance().getPlotSQL().update(sql);
    }
}
