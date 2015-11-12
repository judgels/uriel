package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantOrganizationModel;

public interface ContestContestantOrganizationDao extends Dao<Long, ContestContestantOrganizationModel> {

    ContestContestantOrganizationModel findInContestByContestantJid(String contestJid, String contestantJid);

}
