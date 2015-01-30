package org.iatoki.judgels.uriel;

import java.util.Date;

public final class ContestClarification {

    private long id;

    private String contestJid;

    private String topic;

    private String question;

    private String answer;

    private String asker;

    private String answerer;

    private ContestClarificationStatus status;

    private Date askedTime;

    private Date answeredTime;

    public ContestClarification(long id, String contestJid, String topic, String question, String answer, String asker, ContestClarificationStatus status, Date askedTime) {
        this.id = id;
        this.contestJid = contestJid;
        this.topic = topic;
        this.question = question;
        this.answer = answer;
        this.asker = asker;
        this.status = status;
        this.askedTime = askedTime;
    }

    public ContestClarification(long id, String contestJid, String topic, String question, String answer, String asker, String answerer, ContestClarificationStatus status, Date askedTime, Date answeredTime) {
        this.id = id;
        this.contestJid = contestJid;
        this.topic = topic;
        this.question = question;
        this.answer = answer;
        this.asker = asker;
        this.answerer = answerer;
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

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAsker() {
        return asker;
    }

    public String getAnswerer() {
        return answerer;
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
