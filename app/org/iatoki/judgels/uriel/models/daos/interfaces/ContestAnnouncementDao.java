package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.uriel.models.domains.ContestAnnouncementModel;

import java.util.List;

public interface ContestAnnouncementDao extends Dao<Long, ContestAnnouncementModel> {

    List<ContestAnnouncementModel> findPublishedByContestJidOrderedByUpdateTime(String contestJid);

    List<ContestAnnouncementModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order);

}
