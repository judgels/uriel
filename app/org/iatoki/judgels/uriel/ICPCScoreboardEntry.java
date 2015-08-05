package org.iatoki.judgels.uriel;

import com.beust.jcommander.internal.Lists;

import java.net.URL;
import java.util.List;

public class ICPCScoreboardEntry implements ScoreboardEntry, Comparable<ICPCScoreboardEntry> {

    public int rank;
    public String contestantJid;
    public URL imageURL;
    public int totalAccepted;
    public long totalPenalties;
    public List<Integer> attemptsList;
    public List<Long> penaltyList;
    public List<Boolean> isAcceptedList;

    public ICPCScoreboardEntry() {
        this.isAcceptedList = Lists.newArrayList();
        this.attemptsList = Lists.newArrayList();
        this.penaltyList = Lists.newArrayList();
    }

    @Override
    public int compareTo(ICPCScoreboardEntry o) {
        int ignoringTieBreaker = compareToIgnoringTieBreaker(o);

        if (ignoringTieBreaker != 0) {
            return ignoringTieBreaker;
        }

        int totalAttempts = attemptsList.stream().mapToInt(i -> i).sum();
        int otherTotalAttempts = o.attemptsList.stream().mapToInt(i -> i).sum();

        if (totalAttempts == 0 && otherTotalAttempts == 0) {
            return contestantJid.compareTo(o.contestantJid);
        } else if (totalAttempts == 0) {
            return 1;
        } else if (otherTotalAttempts == 0) {
            return -1;
        } else {
            return Integer.compare(otherTotalAttempts, totalAttempts);
        }
    }

    public int compareToIgnoringTieBreaker(ICPCScoreboardEntry o) {
        if (totalAccepted != o.totalAccepted) {
            return Integer.compare(o.totalAccepted, totalAccepted);
        }

        if (totalPenalties != o.totalPenalties) {
            return Long.compare(totalPenalties, o.totalPenalties);
        }

        long maxPenalty = getMaxPenalty(isAcceptedList, penaltyList);
        long otherMaxPenalty = getMaxPenalty(o.isAcceptedList, o.penaltyList);

        if (maxPenalty != otherMaxPenalty) {
            return Long.compare(maxPenalty, otherMaxPenalty);
        }

        return 0;
    }

    private long getMaxPenalty(List<Boolean> isAcceptedList, List<Long> penaltyList) {
        long result = 0;

        for (int i = 0; i < isAcceptedList.size(); i++) {
            if (isAcceptedList.get(i)) {
                result = Math.max(result, penaltyList.get(i));
            }
        }

        return result;
    }
}
