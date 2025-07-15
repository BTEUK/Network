package net.bteuk.network.utils;

import net.bteuk.network.Network;

/**
 * Represents a tutorial recommendation
 */
public class TutorialRecommendation {
    private final int iRecommendationID;

    private final int iPlotID;

    private final teachingtutorials.tutorialobjects.TutorialRecommendation linkedTutorialRecommendation;

    public TutorialRecommendation(int iRecommendationID, int iPlotID) {
        this.iRecommendationID = iRecommendationID;
        this.iPlotID = iPlotID;
        this.linkedTutorialRecommendation = teachingtutorials.tutorialobjects.TutorialRecommendation.fetchTutorialRecommendationByID(
                Network.getInstance().getTutorialsDBConnection(), Constants.LOGGER, iRecommendationID);
    }

    public teachingtutorials.tutorialobjects.TutorialRecommendation getLinkedTutorialRecommendation() {
        return linkedTutorialRecommendation;
    }

    /**
     * Adds this tutorials recommendation to the plots database
     */
    public void addTutorialRecommendationToDB() {
        String sql = "INSERT INTO tutorial_recommendations (`plot_id`, `recommendation_id`) VALUES (" + this.iPlotID + ", " + this.iRecommendationID + ")";
        Network.getInstance().getPlotSQL().update(sql);
    }
}
