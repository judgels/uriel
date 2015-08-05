package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.uriel.models.entities.ContestTeamModel;

import java.util.Collection;
import java.util.List;

public interface ContestTeamDao extends JudgelsDao<ContestTeamModel> {

    List<ContestTeamModel> findContestTeamModelsByContestJid(String contestJid);

    List<String> findTeamJidsByContestJid(String contestJid);

    List<String> findContestJidsByTeamJids(Collection<String> teamJids);

}
