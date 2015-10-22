package org.iatoki.judgels.uriel;

import com.google.common.collect.Lists;

import java.net.URL;
import java.util.List;

public class ICPCScoreboardEntry implements ScoreboardEntry {

    public enum State {
        NOT_ACCEPTED,
        ACCEPTED,
        FIRST_ACCEPTED
    }

    public int rank;
    public String contestantJid;
    public URL imageURL;
    public int totalAccepted;
    public long totalPenalties;
    public long lastAcceptedPenalty;
    public List<Integer> attemptsList;
    public List<Long> penaltyList;
    public List<Integer> problemStateList;

    public ICPCScoreboardEntry() {
        this.problemStateList = Lists.newArrayList();
        this.attemptsList = Lists.newArrayList();
        this.penaltyList = Lists.newArrayList();
    }
}
