package org.iatoki.judgels.uriel.services.impls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.Page;
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
    public boolean isContestProblemInContestByProblemJidOrAlias(String contestJid, String contestProblemJid, String contestProblemAlias) {
        return ((contestProblemDao.existsByProblemJid(contestJid, contestProblemJid)) || (contestProblemDao.existsByProblemAlias(contestJid, contestProblemAlias)));
    }

    @Override
    public ContestProblem findContestProblemByContestProblemId(long contestProblemId) throws ContestProblemNotFoundException {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);
        if (contestProblemModel != null) {
            return createContestProblemFromModel(contestProblemModel);
        } else {
            throw new ContestProblemNotFoundException("Contest Problem not found.");
        }
    }

    @Override
    public ContestProblem findContestProblemByContestJidAndContestProblemJid(String contestJid, String contestProblemJid) {
        ContestProblemModel contestProblemModel = contestProblemDao.findByProblemJidOrderedByAlias(contestJid, contestProblemJid);
        return createContestProblemFromModel(contestProblemModel);
    }

    @Override
    public List<ContestProblem> findOpenedContestProblemByContestJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findOpenedByContestJidOrderedByAlias(contestJid);
        return Lists.transform(contestProblemModels, m -> createContestProblemFromModel(m));
    }

    @Override
    public Page<ContestProblem> pageContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, String status) {
        ImmutableMap.Builder<SingularAttribute<? super ContestProblemModel, String>, String> filterColumnsBuilder = ImmutableMap.builder();
        filterColumnsBuilder.put(ContestProblemModel_.contestJid, contestJid);
        if (status != null) {
            filterColumnsBuilder.put(ContestProblemModel_.status, status);
        }
        Map<SingularAttribute<? super ContestProblemModel, String>, String> filterColumns = filterColumnsBuilder.build();

        long totalPages = contestProblemDao.countByFilters(filterString, filterColumns, ImmutableMap.of());
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findSortedByFilters(orderBy, orderDir, filterString, filterColumns, ImmutableMap.of(), pageIndex * pageSize, pageSize);
        List<ContestProblem> contestProblems = Lists.transform(contestProblemModels, m -> createContestProblemFromModel(m));

        return new Page<>(contestProblems, totalPages, pageIndex, pageSize);
    }

    @Override
    public Page<ContestProblem> pageUsedContestProblemsByContestJid(String contestJid, long pageIndex, long pageSize) {
        long totalRows = contestProblemDao.countValidByContestJid(contestJid);

        List<ContestProblemModel> contestProblemModels = contestProblemDao.findUsedByContestJidOrderedByStatusAndThenAlias(contestJid, pageIndex * pageSize, pageSize);
        List<ContestProblem> contestProblems = Lists.transform(contestProblemModels, m -> createContestProblemFromModel(m));

        return new Page<>(contestProblems, totalRows, pageIndex, pageSize);
    }

    @Override
    public Map<String, String> findProblemJidToAliasMapByContestJid(String contestJid) {
        List<ContestProblemModel> contestProblemModels = contestProblemDao.findByContestJid(contestJid);

        Map<String, String> map = Maps.newLinkedHashMap();

        for (ContestProblemModel model : contestProblemModels) {
            map.put(model.problemJid, model.alias);
        }

        return map;
    }

    @Override
    public void createContestProblem(long contestId, String problemJid, String problemSecret, String alias, long submissionsLimit, ContestProblemStatus status) {
        ContestModel contestModel = contestDao.findById(contestId);

        ContestProblemModel contestProblemModel = new ContestProblemModel();
        contestProblemModel.contestJid = contestModel.jid;
        contestProblemModel.problemJid = problemJid;
        contestProblemModel.problemSecret = problemSecret;
        contestProblemModel.alias = alias;
        contestProblemModel.submissionsLimit = submissionsLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.persist(contestProblemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void updateContestProblem(long contestProblemId, String alias, long submissionsLimit, ContestProblemStatus status) {
        ContestProblemModel contestProblemModel = contestProblemDao.findById(contestProblemId);
        contestProblemModel.alias = alias;
        contestProblemModel.submissionsLimit = submissionsLimit;
        contestProblemModel.status = status.name();

        contestProblemDao.edit(contestProblemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    private ContestProblem createContestProblemFromModel(ContestProblemModel contestProblemModel) {
        return new ContestProblem(contestProblemModel.id, contestProblemModel.contestJid, contestProblemModel.problemJid, contestProblemModel.problemSecret, contestProblemModel.alias, contestProblemModel.submissionsLimit, ContestProblemStatus.valueOf(contestProblemModel.status));
    }
}
