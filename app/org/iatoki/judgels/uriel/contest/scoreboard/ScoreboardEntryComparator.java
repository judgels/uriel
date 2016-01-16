package org.iatoki.judgels.uriel.contest.scoreboard;

import java.util.Comparator;

public interface ScoreboardEntryComparator<T extends ScoreboardEntry> extends Comparator<T> {

    int compareWithoutTieBreakerForEqualRanks(T entry1, T entry2);

    int compareWithTieBreakerForEqualRanks(T entry1, T entry2);
}
