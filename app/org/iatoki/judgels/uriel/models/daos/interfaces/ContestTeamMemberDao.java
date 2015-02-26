package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestScoreModel;

import java.util.List;

public interface ContestTeamMemberDao extends Dao<Long, ContestScoreModel> {

    List<ContestScoreModel> findByContestJid(String contestJid);

    boolean isExistByContestJidAndContestantJid(String contestJid, String contestantJid);
}
