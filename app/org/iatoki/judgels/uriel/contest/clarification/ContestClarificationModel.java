package org.iatoki.judgels.uriel.contest.clarification;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_clarification")
@JidPrefix("COCL")
public final class ContestClarificationModel extends AbstractJudgelsModel {

    public String contestJid;

    public String topicJid;

    public String title;

    @Column(columnDefinition = "TEXT")
    public String question;

    @Column(columnDefinition = "TEXT")
    public String answer;

    public String status;
}
