package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.domains.AbstractJudgelsModel;

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
