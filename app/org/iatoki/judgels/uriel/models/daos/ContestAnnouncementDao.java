package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel;

import java.util.List;

public interface ContestAnnouncementDao extends Dao<Long, ContestAnnouncementModel> {

    List<ContestAnnouncementModel> findPublishedByContestJidOrderedByUpdateTime(String contestJid);

    List<Long> findAllPublishedAnnouncementIdInContest(String contestJid);
}
