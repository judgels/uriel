package org.iatoki.judgels.uriel;

import org.iatoki.judgels.gabriel.Verdict;
import org.iatoki.judgels.sandalphon.commons.Submission;

public final class ContestSubmission implements Submission {

    private final long id;
    private final String jid;
    private final String problemJid;
    private final String contestJid;
    private final String authorJid;
    private final String gradingLanguage;
    private final String gradingEngine;
    private final long timestamp;
    private final Verdict verdict;
    private final int score;
    private final String details;

    public ContestSubmission(long id, String jid, String problemJid, String contestJid, String authorJid, String gradingLanguage, String gradingEngine, long timestamp, Verdict verdict, int score, String details) {
        this.id = id;
        this.jid = jid;
        this.problemJid = problemJid;
        this.contestJid = contestJid;
        this.authorJid = authorJid;
        this.gradingLanguage = gradingLanguage;
        this.gradingEngine = gradingEngine;
        this.timestamp = timestamp;
        this.verdict = verdict;
        this.score = score;
        this.details = details;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getJid() {
        return jid;
    }

    @Override
    public String getProblemJid() {
        return problemJid;
    }

    @Override
    public String getContestJid() {
        return contestJid;
    }

    @Override
    public String getAuthorJid() {
        return authorJid;
    }

    @Override
    public String getGradingLanguage() {
        return gradingLanguage;
    }

    @Override
    public String getGradingEngine() {
        return gradingEngine;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Verdict getVerdict() {
        return verdict;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public String getDetails() {
        return details;
    }
}
