package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.commons.models.JidPrefix;
import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel;

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
