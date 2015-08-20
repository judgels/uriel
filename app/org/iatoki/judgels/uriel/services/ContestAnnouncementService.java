package org.iatoki.judgels.uriel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementNotFoundException;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;

import java.util.Collection;

public interface ContestAnnouncementService {

    ContestAnnouncement findContestAnnouncementById(long contestAnnouncementId) throws ContestAnnouncementNotFoundException;

    Page<ContestAnnouncement> getPageOfAnnouncementsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status);

    long countUnreadAnnouncementsInContest(String userJid, String contestJid);

    void createContestAnnouncement(long contestId, String title, String content, ContestAnnouncementStatus status);

    void updateContestAnnouncement(long contestAnnouncementId, String title, String content, ContestAnnouncementStatus status);

    void readContestAnnouncements(String userJid, Collection<String> contestAnnouncementJids);
}
