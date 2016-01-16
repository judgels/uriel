package org.iatoki.judgels.uriel.contest.team.member;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_team_member")
public final class ContestTeamMemberModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String teamJid;

    public String memberJid;
}
