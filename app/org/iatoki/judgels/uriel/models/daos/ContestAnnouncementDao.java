package org.iatoki.judgels.uriel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel;

import java.util.List;

public interface ContestAnnouncementDao extends JudgelsDao<ContestAnnouncementModel> {

    List<String> getPublishedJidsInContest(String contestJid);
}
