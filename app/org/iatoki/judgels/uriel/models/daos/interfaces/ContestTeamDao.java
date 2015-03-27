package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.uriel.models.domains.ContestTeamModel;

import java.util.Collection;
import java.util.List;

public interface ContestTeamDao extends JudgelsDao<ContestTeamModel> {

    List<ContestTeamModel> findContestTeamModelsByContestJid(String contestJid);

    List<String> findTeamJidsByContestJid(String contestJid);

    List<String> findContestJidsByTeamJids(Collection<String> teamJids);

}
