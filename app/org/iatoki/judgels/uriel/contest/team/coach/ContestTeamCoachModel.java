package org.iatoki.judgels.uriel.contest.team.coach;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_team_coach")
public final class ContestTeamCoachModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String teamJid;

    public String coachJid;
}
