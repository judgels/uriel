package org.iatoki.judgels.uriel;

public final class UsingLastAffectingPenaltyIOIScoreboardEntryComparator extends AbstractScoreboardEntryComparator<IOIScoreboardEntry> {

    @Override
    public int compareWithoutTieBreakerForEqualRanks(IOIScoreboardEntry entry1, IOIScoreboardEntry entry2) {
        if (entry1.totalScores != entry2.totalScores) {
            return Integer.compare(entry2.totalScores, entry1.totalScores);
        }

        return Long.compare(entry1.lastAffectingPenalty, entry2.lastAffectingPenalty);
    }

    @Override
    public int compareWithTieBreakerForEqualRanks(IOIScoreboardEntry entry1, IOIScoreboardEntry entry2) {
        int submittedProblems1 = (int) entry1.scores.stream().filter(s -> s != null).count();
        int submittedProblems2 = (int) entry2.scores.stream().filter(s -> s != null).count();

        // prioritize entry which has more number of problems that have at least one submission
        if (submittedProblems1 != submittedProblems2) {
            return Integer.compare(submittedProblems2, submittedProblems1);
        }

        return entry1.contestantJid.compareTo(entry2.contestantJid);
    }
}
