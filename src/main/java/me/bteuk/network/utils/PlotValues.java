package me.bteuk.network.utils;

public class PlotValues {

    //Returns the plot difficulty name.
    public static String difficultyName(int difficulty) {

        switch (difficulty) {

            case 1:
                return "Easy";
            case 2:
                return "Normal";
            case 3:
                return "Hard";
            default:
                return null;

        }
    }

    //Returns the plot size name.
    public static String sizeName(int size) {

        switch (size) {

            case 1:
                return "Small";
            case 2:
                return "Medium";
            case 3:
                return "Large";
            default:
                return null;

        }
    }
}
