package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestAnnouncement;
import org.iatoki.judgels.uriel.ContestAnnouncementNotFoundException;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.ContestReadType;
import org.iatoki.judgels.uriel.models.daos.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestReadDao;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel_;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel;
import org.iatoki.judgels.uriel.services.ContestAnnouncementService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Singleton
@Named("contestAnnouncementService")
public final class ContestAnnouncementServiceImpl implements ContestAnnouncementService {

    private final ContestDao contestDao;
    private final ContestAnnouncementDao contestAnnouncementDao;
    private final ContestReadDao contestReadDao;

    @Inject
    public ContestAnnouncementServiceImpl(ContestDao contestDao, ContestAnnouncementDao contestAnnouncementDao, ContestReadDao contestReadDao) {
        this.contestDao = contestDao;
        this.contestAnnouncementDao = contestAnnouncementDao;
        this.contestReadDao = contestReadDao;
    }

    @Override
    public ContestAnnouncement findContestAnnouncementByContestAnnouncementId(long contestAnnouncementId) throws ContestAnnouncementNotFoundException {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findById(contestAnnouncementId);
        if (contestAnnouncementModel != null) {
            return createContestAnnouncementFromModel(contestAnnouncementModel);
        } else {
            throw new ContestAnnouncementNotFoundException("Contest Announcement not found.");
        }
    }

    @Override
    public Page<ContestAnnouncement> pageContestAnnouncementsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status) {
        ImmutableMap.Builder<SingularAttribute<? super ContestAnnouncementModel, String>, String> filterColumnsBuilder = ImmutableMap.builder();
        filterColumnsBuilder.put(ContestAnnouncementModel_.contestJid, contestJid);
        if (status != null) {
            filterColumnsBuilder.put(ContestAnnouncementModel_.status, status);
        }
        Map<SingularAttribute<? super ContestAnnouncementModel, String>, String> filterColumns = filterColumnsBuilder.build();

        long totalPages = contestAnnouncementDao.countByFilters(filterString, filterColumns, ImmutableMap.of());
        List<ContestAnnouncementModel> contestAnnouncementModels = contestAnnouncementDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, ImmutableMap.of(), pageIndex, pageIndex * pageSize);
        List<ContestAnnouncement> contestAnnouncements = Lists.transform(contestAnnouncementModels, m -> createContestAnnouncementFromModel(m));

        return new Page<>(contestAnnouncements, totalPages, pageIndex, pageSize);
    }

    @Override
    public long getUnreadContestAnnouncementsCount(String userJid, String contestJid) {
        List<String> announcementJids = contestAnnouncementDao.findAllPublishedAnnouncementJidInContest(contestJid);
        if (!announcementJids.isEmpty()) {
            return (announcementJids.size() - contestReadDao.countReadByUserJidAndTypeAndJidList(userJid, ContestReadType.ANNOUNCEMENT.name(), announcementJids));
        } else {
            return 0;
        }
    }

    @Override
    public void createContestAnnouncement(long contestId, String title, String content, ContestAnnouncementStatus status) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestAnnouncementModel contestAnnouncementModel = new ContestAnnouncementModel();
        contestAnnouncementModel.contestJid = contestModel.jid;
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.content = content;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.persist(contestAnnouncementModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestAnnouncement(long contestAnnouncementId, String title, String content, ContestAnnouncementStatus status) {
        ContestAnnouncementModel contestAnnouncementModel = contestAnnouncementDao.findById(contestAnnouncementId);
        contestAnnouncementModel.title = title;
        contestAnnouncementModel.content = content;
        contestAnnouncementModel.status = status.name();

        contestAnnouncementDao.edit(contestAnnouncementModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void readContestAnnouncements(String userJid, List<String> contestAnnouncementJids) {
        for (String contestAnnouncementJid : contestAnnouncementJids) {
            if (!contestReadDao.existByUserJidAndTypeAndJid(userJid, ContestReadType.ANNOUNCEMENT.name(), contestAnnouncementJid)) {
                ContestReadModel contestReadModel = new ContestReadModel();
                contestReadModel.userJid = userJid;
                contestReadModel.type = ContestReadType.ANNOUNCEMENT.name();
                contestReadModel.readJid = contestAnnouncementJid;

                contestReadDao.persist(contestReadModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
            }
        }
    }

    private ContestAnnouncement createContestAnnouncementFromModel(ContestAnnouncementModel contestAnnouncementModel) {
        return new ContestAnnouncement(contestAnnouncementModel.id, contestAnnouncementModel.jid, contestAnnouncementModel.contestJid, contestAnnouncementModel.title, contestAnnouncementModel.content, contestAnnouncementModel.userCreate, ContestAnnouncementStatus.valueOf(contestAnnouncementModel.status), new Date(contestAnnouncementModel.timeUpdate));
    }
}
