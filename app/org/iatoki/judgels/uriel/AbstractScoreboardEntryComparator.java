package org.iatoki.judgels.uriel;

public abstract class AbstractScoreboardEntryComparator<T extends ScoreboardEntry> implements ScoreboardEntryComparator<T> {

    @Override
    public final int compare(T entry1, T entry2) {
        int withoutTieBreaker = compareWithoutTieBreakerForEqualRanks(entry1, entry2);

        if (withoutTieBreaker != 0) {
            return withoutTieBreaker;
        }

        return compareWithTieBreakerForEqualRanks(entry1, entry2);
    }
}
