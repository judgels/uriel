package org.iatoki.judgels.uriel;

import com.google.common.collect.Lists;

import java.net.URL;
import java.util.List;

public final class IOIScoreboardEntry implements ScoreboardEntry {

    public int rank;
    public String contestantJid;
    public URL imageURL;
    public List<Integer> scores;
    public int totalScores;

    public IOIScoreboardEntry() {
        scores = Lists.newArrayList();
    }
}
