package org.iatoki.judgels.uriel;

import org.iatoki.judgels.uriel.commons.ScoreEntry;

public final class ContestScore {
    public ContestScore(long id, String contestJid, String contestantJid, ScoreEntry scores) {
        this.id = id;
        this.contestJid = contestJid;
        this.contestantJid = contestantJid;
        this.scores = scores;
    }

    private long id;

    private String contestJid;

    private String contestantJid;

    private ScoreEntry scores;

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getContestantJid() {
        return contestantJid;
    }

    public ScoreEntry getScores() {
        return scores;
    }
}
