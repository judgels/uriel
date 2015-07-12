package org.iatoki.judgels.uriel.adapters.impls;

import org.iatoki.judgels.uriel.ContestStyle;
import org.iatoki.judgels.uriel.adapters.ScoreAdapter;

public final class ScoreAdapters {
    private ScoreAdapters() {
        // prevent instantiation
    }

    public static ScoreAdapter fromContestStyle(ContestStyle style) {
        switch (style) {
            case IOI:
                return new IOIScoreAdapter();
            default:
                throw new IllegalArgumentException();
        }
    }
}
