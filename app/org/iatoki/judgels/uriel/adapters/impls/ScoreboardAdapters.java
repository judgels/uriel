package org.iatoki.judgels.uriel.adapters.impls;

import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.adapters.ScoreboardAdapter;

public final class ScoreboardAdapters {
    private ScoreboardAdapters() {
        // prevent instantiation
    }

    public static ScoreboardAdapter fromContestStyle(ContestStyle style) {
        switch (style) {
            case IOI:
                return new IOIScoreboardAdapter();
            default:
                throw new IllegalArgumentException();
        }
    }
}
