package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_problem")
public final class ContestProblemModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String problemJid;

    public String problemSecret;

    public String alias;

    public String status;

    public long submissionsLimit;
}