package org.iatoki.judgels.uriel;

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
