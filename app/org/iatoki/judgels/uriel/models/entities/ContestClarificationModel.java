package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_clarification")
public final class ContestClarificationModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String topicJid;

    public String title;

    @Column(columnDefinition = "TEXT")
    public String question;

    @Column(columnDefinition = "TEXT")
    public String answer;

    public String status;

}
