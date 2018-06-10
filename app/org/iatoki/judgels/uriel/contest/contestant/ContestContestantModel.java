package org.iatoki.judgels.uriel.contest.contestant;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "uriel_contest_contestant")
public final class ContestContestantModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String userJid;

    public String status;

    @Temporal(TemporalType.TIMESTAMP)
    public Date contestStartTime;
}
