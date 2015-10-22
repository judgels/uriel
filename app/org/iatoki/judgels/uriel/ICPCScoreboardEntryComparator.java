package org.iatoki.judgels.uriel;

public final class ICPCScoreboardEntryComparator extends AbstractScoreboardEntryComparator<ICPCScoreboardEntry> {

    @Override
    public int compareWithoutTieBreakerForEqualRanks(ICPCScoreboardEntry entry1, ICPCScoreboardEntry entry2) {
        if (entry1.totalAccepted != entry2.totalAccepted) {
            return Integer.compare(entry2.totalAccepted, entry1.totalAccepted);
        }

        if (entry1.totalPenalties != entry2.totalPenalties) {
            return Long.compare(entry1.totalPenalties, entry2.totalPenalties);
        }

        return Long.compare(entry1.lastAcceptedPenalty, entry2.lastAcceptedPenalty);
    }

    @Override
    public int compareWithTieBreakerForEqualRanks(ICPCScoreboardEntry entry1, ICPCScoreboardEntry entry2) {
        int totalAttempts1 = entry1.attemptsList.stream().mapToInt(i -> i).sum();
        int totalAttempts2 = entry2.attemptsList.stream().mapToInt(i -> i).sum();

        if (totalAttempts1 == 0 && totalAttempts2 == 0) {
            return entry1.contestantJid.compareTo(entry2.contestantJid);
        } else if (totalAttempts1 == 0) {
            return 1;
        } else if (totalAttempts2 == 0) {
            return -1;
        } else {
            return Integer.compare(totalAttempts2, totalAttempts1);
        }
    }
}
