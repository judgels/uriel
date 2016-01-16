package org.iatoki.judgels.uriel.contest.supervisor;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_supervisor")
public final class ContestSupervisorModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String userJid;

    @Column(columnDefinition = "TEXT")
    public String permission;
}
