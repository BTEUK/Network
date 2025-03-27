package net.bteuk.network.utils.plotsystem;

import lombok.Getter;

@Getter
public enum PlotDifficulty {

    EASY(1),
    NORMAL(2),
    HARD(3);

    private final int difficulty;

    PlotDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
