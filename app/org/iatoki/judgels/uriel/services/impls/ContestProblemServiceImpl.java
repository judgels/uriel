package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.uriel.ContestProblem;
import org.iatoki.judgels.uriel.ContestProblemNotFoundException;
import org.iatoki.judgels.uriel.ContestProblemStatus;
import org.iatoki.judgels.uriel.models.daos.ContestDao;
import org.iatoki.judgels.uriel.models.daos.ContestProblemDao;
import org.iatoki.judgels.uriel.models.entities.ContestModel;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel;
import org.iatoki.judgels.uriel.models.entities.ContestProblemModel_;
import org.iatoki.judgels.uriel.services.ContestProblemService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.Map;

@Singleton
@Named("contestProblemService")
public final class ContestProblemServiceImpl implements ContestProblemService {

    private final ContestDao contestDao;
    private final ContestProblemDao contestProblemDao;

    @Inject
    public ContestProblemServiceImpl(ContestDao contestDao, ContestProblemDao contestProblemDao) {
        this.contestDao = contestDao;
        this.contestProblemDao = contestProblemDao;
    }

    @Override
    public boolean isProblemInContestByJidOrAlias(String contestJid, String contestProblemJid, String contestProblemAlias) {
        return ((contestProblemDao.existsInContestByJid(contestJid, contestProblemJid)) || (contestProblemDao.existsInContestByAlias(contestJid, contestProblemAlias)));
    }

    @Override
    public ContestProblem findContestProblemById(long contestProblemId) throws ContestProblemNotFoundException {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);
        if (contestProblemModel == null) {
            throw new ContestProblemNotFoundException("Contest Problem not found.");
        }

        return ContestProblemServiceUtils.createContestProblemFromModel(contestProblemModel);
    }

    @Override
    public ContestProblem findContestProblemInContestAndJid(String contestJid, String contestProblemJid) {
        ContestProblemModel contestProblemModel = contestProblemDao.findInContestByJid(contestJid, contestProblemJid);
        return ContestProblemServiceUtils.createContestProblemFromModel(contestProblemModel);
    }

    @Override
    public List<ContestProblem> getOpenedProblemsInContest(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.getOpenedInContest(contestJid);
        return Lists.transform(contestProblemModels, m -> ContestProblemServiceUtils.createContestProblemFromModel(m));
    }

    @Override
    public Page<ContestProblem> getPageOfProblemsInContest(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status) {
        ImmutableMap.Builder<SingularAttribute<? super ContestProblemModel, String>, String> filterColumnsBuilder = ImmutableMap.builder();
        filterColumnsBuilder.put(ContestProblemModel_.contestJid, contestJid);
        if (status != null) {
            filterColumnsBuilder.put(ContestProblemModel_.status, status);
        }
        Map<SingularAttribute<? super ContestProblemModel, String>, String> filterColumns = filterColumnsBuilder.build();

        long totalPages = contestProblemDao.countByFilters(filterString, filterColumns, ImmutableMap.of());
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<ContestProblem> contestProblems = Lists.transform(contestProblemModels, m -> ContestProblemServiceUtils.createContestProblemFromModel(m));

        return new Page<>(contestProblems, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<ContestProblem> getPageOfUsedProblemsInContest(String contestJid, long pageIndex, long pageSize) {
        long totalRows = contestProblemDao.countValidInContest(contestJid);

        List<ContestProblemModel> contestProblemModels = contestProblemDao.getUsedInContestWithLimit(contestJid, pageIndex * pageSize, pageSize);
        List<ContestProblem> contestProblems = Lists.transform(contestProblemModels, m -> ContestProblemServiceUtils.createContestProblemFromModel(m));

        return new Page<>(contestProblems, totalRows, pageIndex, pageSize);
    }

    @Override
    public Map<String, String> getMappedJidToAliasInContest(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.getAllInContest(contestJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (ContestProblemModel model : contestProblemModels) {
            map.put(model.problemJid, model.alias);
        }

        return map;
    }

    @Override
    public void createContestProblem(String contestJid, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status, String userJid, String userIpAddress) {
        ContestModel contestModel = contestDao.findByJid(contestJid);

        ContestProblemModel contestProblemModel = new ContestProblemModel();
        contestProblemModel.contestJid = contestModel.jid;
        contestProblemModel.problemJid = problemJid;
        contestProblemModel.problemSecret = problemSecret;
        contestProblemModel.alias = alias;
        contestProblemModel.submissionsLimit = submissionsLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.persist(contestProblemModel, userJid, userIpAddress);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void updateContestProblem(long contestProblemId, String alias, long submissionsLimit, ContestProblemStatus status, String userJid, String userIpAddress) {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);
        contestProblemModel.alias = alias;
        contestProblemModel.submissionsLimit = submissionsLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.edit(contestProblemModel, userJid, userIpAddress);

        ContestModel contestModel = contestDao.findByJid(contestProblemModel.contestJid);

        contestDao.edit(contestModel, userJid, userIpAddress);
    }

    @Override
    public void deleteContestProblem(long contestProblemId) {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);

        contestProblemDao.remove(contestProblemModel);
    }
}
