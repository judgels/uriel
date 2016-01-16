package org.iatoki.judgels.uriel.contest.scoreboard;

import org.iatoki.judgels.uriel.contest.scoreboard.icpc.ICPCScoreboardAdapter;
import org.iatoki.judgels.uriel.contest.scoreboard.ioi.IOIScoreboardAdapter;
import org.iatoki.judgels.uriel.contest.style.ContestStyle;

public final class ScoreboardAdapters {

    private ScoreboardAdapters() {
        // prevent instantiation
    }

    public static ScoreboardAdapter fromContestStyle(ContestStyle style) {
        switch (style) {
            case IOI:
                return new IOIScoreboardAdapter();
            case ICPC:
                return new ICPCScoreboardAdapter();
            default:
                throw new IllegalArgumentException();
        }
    }
}
