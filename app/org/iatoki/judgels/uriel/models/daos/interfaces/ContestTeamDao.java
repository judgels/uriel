package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.uriel.models.domains.ContestTeamModel;

import java.util.List;

public interface ContestTeamDao extends JudgelsDao<ContestTeamModel> {

    List<String> findAllTeamJidsInContest(String contestJid);

}
