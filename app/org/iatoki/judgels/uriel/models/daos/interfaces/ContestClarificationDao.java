package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;

import java.util.List;

public interface ContestClarificationDao extends Dao<Long, ContestClarificationModel> {

    long countUnansweredClarificationByContestJid(String contestJid);

    List<Long> findAllAnsweredClarificationIdInContestByUserJid(String contestJid, String userJid);

}
