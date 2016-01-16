package org.iatoki.judgels.uriel.contest.announcement;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

import java.util.List;

@ImplementedBy(ContestAnnouncementHibernateDao.class)
public interface ContestAnnouncementDao extends JudgelsDao<ContestAnnouncementModel> {

    List<String> getPublishedJidsInContest(String contestJid);
}
