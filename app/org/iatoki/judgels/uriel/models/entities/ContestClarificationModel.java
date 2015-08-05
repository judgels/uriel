package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

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
