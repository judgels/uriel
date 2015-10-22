package org.iatoki.judgels.uriel;

import com.google.common.collect.Lists;

import java.net.URL;
import java.util.List;

public class ICPCScoreboardEntry implements ScoreboardEntry {

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
}
