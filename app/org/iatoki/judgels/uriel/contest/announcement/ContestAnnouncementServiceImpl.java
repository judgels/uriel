package org.iatoki.judgels.uriel.contest.announcement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.contest.ContestReadType;
import org.iatoki.judgels.uriel.contest.ContestDao;
import org.iatoki.judgels.uriel.user.UserReadDao;
import org.iatoki.judgels.uriel.contest.ContestModel;
import org.iatoki.judgels.uriel.user.UserReadModel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Singleton
public final class ContestAnnouncementServiceImpl implements ContestAnnouncementService {

    private final ContestAnnouncementDao contestAnnouncementDao;
    private final ContestDao contestDao;
    private final UserReadDao userReadDao;

    @Inject
    public ContestAnnouncementServiceImpl(ContestAnnouncementDao contestAnnouncementDao, ContestDao contestDao, UserReadDao userReadDao) {
        this.contestAnnouncementDao = contestAnnouncementDao;
        this.contestDao = contestDao;
        this.userReadDao = userReadDao;
    }

    @Override
    public ContestAnnouncement findContestAnnouncementById(long contestAnnouncementId) throws ContestAnnouncementNotFoundException {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findById(contestAnnouncementId);
        if (contestAnnouncementModel == null) {
            throw new ContestAnnouncementNotFoundException("Contest Announcement not found.");
        }

        return createContestAnnouncementFromModel(contestAnnouncementModel);
    }

    @Override
    public Page<ContestAnnouncement> getPageOfAnnouncementsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status) {
        ImmutableMap.Builder<SingularAttribute<? super ContestAnnouncementModel, ? extends Object>, String> filterColumnsBuilder = ImmutableMap.builder();
        filterColumnsBuilder.put(ContestAnnouncementModel_.contestJid, contestJid);
        if (status != null) {
            filterColumnsBuilder.put(ContestAnnouncementModel_.status, status);
        }
        Map<SingularAttribute<? super ContestAnnouncementModel, ? extends Object>, String> filterColumns = filterColumnsBuilder.build();

        long totalPages = contestAnnouncementDao.countByFiltersEq(filterString, filterColumns);
        List<ContestAnnouncementModel> contestAnnouncementModels = contestAnnouncementDao.findSortedByFiltersEq(orderBy, orderDir, filterString, filterColumns, pageIndex, pageIndex * pageSize);
        List<ContestAnnouncement> contestAnnouncements = Lists.transform(contestAnnouncementModels, m -> createContestAnnouncementFromModel(m));

        return new Page<>(contestAnnouncements, totalPages, pageIndex, pageSize);
    }

    @Override
    public long countUnreadAnnouncementsInContest(String userJid, String contestJid) {
        List<String> announcementJids = contestAnnouncementDao.getPublishedJidsInContest(contestJid);
        if (announcementJids.isEmpty()) {
            return 0;
        }

        return (announcementJids.size() - userReadDao.countReadByUserJidAndTypeAndJids(userJid, ContestReadType.ANNOUNCEMENT.name(), announcementJids));
    }

    @Override
    public ContestAnnouncement createContestAnnouncement(String contestJid, String title, String content, ContestAnnouncementStatus status, String userJid, String userIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ContestAnnouncementModel contestAnnouncementModel = new ContestAnnouncementModel();
        contestAnnouncementModel.contestJid = contestModel.jid;
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.content = content;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.persist(contestAnnouncementModel, userJid, userIpAddress);

        contestDao.edit(contestModel, userJid, userIpAddress);

        return createContestAnnouncementFromModel(contestAnnouncementModel);
    }

    @Override
    public void updateContestAnnouncement(String contestAnnouncementJid, String title, String content, ContestAnnouncementStatus status, String userJid, String userIpAddress) {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findByJid(contestAnnouncementJid);
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.content = content;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.edit(contestAnnouncementModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestAnnouncementModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void readContestAnnouncements(String userJid, Collection<String> contestAnnouncementJids, String userIpAddress) {
        for (String contestAnnouncementJid : contestAnnouncementJids) {
            if (!userReadDao.existsByUserJidAndTypeAndJid(userJid, ContestReadType.ANNOUNCEMENT.name(), contestAnnouncementJid)) {
                UserReadModel contestReadModel = new UserReadModel();
                contestReadModel.userJid = userJid;
                contestReadModel.type = ContestReadType.ANNOUNCEMENT.name();
                contestReadModel.readJid = contestAnnouncementJid;

                userReadDao.persist(contestReadModel, userJid, userIpAddress);
            }
        }
    }

    private static ContestAnnouncement createContestAnnouncementFromModel(ContestAnnouncementModel contestAnnouncementModel) {
        return new ContestAnnouncement(contestAnnouncementModel.id, contestAnnouncementModel.jid, contestAnnouncementModel.contestJid, contestAnnouncementModel.title, contestAnnouncementModel.content, contestAnnouncementModel.createdBy, ContestAnnouncementStatus.valueOf(contestAnnouncementModel.status), contestAnnouncementModel.updatedAt);
    }
}
