package org.iatoki.judgels.uriel.contest.scoreboard.ioi;

import com.google.common.collect.Lists;
import org.iatoki.judgels.uriel.contest.scoreboard.ScoreboardEntry;

import java.net.URL;
import java.util.List;

public final class OldIOIScoreboardEntry implements ScoreboardEntry {

    public int rank;
    public String contestantJid;
    public URL imageURL;
    public List<Integer> scores;
    public int totalScores;
    public long lastAffectingPenalty;

    public OldIOIScoreboardEntry() {
        scores = Lists.newArrayList();
    }
}
