package org.iatoki.judgels.uriel.contest.scoreboard;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_scoreboard")
public final class ContestScoreboardModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String type;

    @Column(columnDefinition = "LONGTEXT")
    public String scoreboard;

    public long time;
}
