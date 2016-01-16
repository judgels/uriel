package org.iatoki.judgels.uriel.contest.announcement;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

import java.util.Collection;

@ImplementedBy(ContestAnnouncementServiceImpl.class)
public interface ContestAnnouncementService {

    ContestAnnouncement findContestAnnouncementById(long contestAnnouncementId) throws ContestAnnouncementNotFoundException;

    Page<ContestAnnouncement> getPageOfAnnouncementsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    long countUnreadAnnouncementsInContest(String userJid, String contestJid);

    ContestAnnouncement createContestAnnouncement(String contestJid, String title, String content, ContestAnnouncementStatus status, String userJid, String userIpAddress);

    void updateContestAnnouncement(String contestAnnouncementJid, String title, String content, ContestAnnouncementStatus status, String userJid, String userIpAddress);

    void readContestAnnouncements(String userJid, Collection<String> contestAnnouncementJids, String userIpAddress);
}
