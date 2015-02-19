package org.iatoki.judgels.uriel;

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
