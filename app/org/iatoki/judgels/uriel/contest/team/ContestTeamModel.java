package org.iatoki.judgels.uriel.contest.team;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_team")
@JidPrefix("TEAM")
public final class ContestTeamModel extends AbstractJudgelsModel {

    public String contestJid;

    public String name;

    public String teamImageName;

    public long contestStartTime;
}
