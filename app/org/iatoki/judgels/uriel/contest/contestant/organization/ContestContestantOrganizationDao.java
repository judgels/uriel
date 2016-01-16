package org.iatoki.judgels.uriel.contest.contestant.organization;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(ContestContestantOrganizationHibernateDao.class)
public interface ContestContestantOrganizationDao extends Dao<Long, ContestContestantOrganizationModel> {

    ContestContestantOrganizationModel findInContestByContestantJid(String contestJid, String contestantJid);

}
