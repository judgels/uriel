package org.iatoki.judgels.uriel;

import java.util.Date;

public final class ContestClarification {

    private final long id;

    private final String contestJid;

    private final String topic;

    private final String title;

    private final String question;

    private final String answer;

    private final String askerJid;

    private final String answererJid;

    private final ContestClarificationStatus status;

    private final Date askedTime;

    private final Date answeredTime;

    public ContestClarification(long id, String contestJid, String topic, String title, String question, String answer, String askerJid, String answererJid, ContestClarificationStatus status, Date askedTime, Date answeredTime) {
        this.id = id;
        this.contestJid = contestJid;
        this.topic = topic;
        this.title = title;
        this.question = question;
        this.answer = answer;
        this.askerJid = askerJid;
        this.answererJid = answererJid;
        this.status = status;
        this.askedTime = askedTime;
        this.answeredTime = answeredTime;
    }

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getTopic() {
        return topic;
    }

    public String getTitle() {
        return title;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAskerJid() {
        return askerJid;
    }

    public String getAnswererJid() {
        return answererJid;
    }

    public ContestClarificationStatus getStatus() {
        return status;
    }

    public Date getAskedTime() {
        return askedTime;
    }

    public Date getAnsweredTime() {
        return answeredTime;
    }
}
