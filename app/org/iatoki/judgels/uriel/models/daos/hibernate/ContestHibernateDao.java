package org.iatoki.judgels.uriel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.ContestScope;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;
import org.iatoki.judgels.uriel.models.domains.ContestModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.List;

public final class ContestHibernateDao extends AbstractJudgelsHibernateDao<ContestModel> implements ContestDao {

    public ContestHibernateDao() {
        super(ContestModel.class);
    }

    @Override
    public List<ContestModel> getRunningContests(long timeNow) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModel> query = cb.createQuery(getModelClass());
        Root<ContestModel> root = query.from(getModelClass());

        query
            .where(cb.and(
                    cb.le(root.get(ContestModel_.startTime), timeNow),
                    cb.ge(root.get(ContestModel_.endTime), timeNow)
            ));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestModel> getRunningContestsWithinContestJids(long timeNow, Collection<String> contestJids) {
        if (contestJids.size() == 0) {
            return ImmutableList.of();
        } else {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<ContestModel> query = cb.createQuery(getModelClass());
            Root<ContestModel> root = query.from(getModelClass());

            query
                .where(cb.and(
                    cb.le(root.get(ContestModel_.startTime), timeNow),
                    cb.ge(root.get(ContestModel_.endTime), timeNow),
                    root.get(ContestModel_.jid).in(contestJids)
                ));

            return JPA.em().createQuery(query).getResultList();
        }
    }

    @Override
    public long countContestsWithinContestJidsOrIsRunningPublic(String filterString, Collection<String> contestJids, long timeNow) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestModel> root = query.from(getModelClass());

        if (contestJids.size() == 0) {
            query.select(cb.count(root)).where(cb.and(
                cb.and(
                    cb.equal(root.get(ContestModel_.scope), ContestScope.PUBLIC.name()),
                    cb.ge(root.get(ContestModel_.endTime), timeNow)
                ),
                cb.or(
                    cb.like(root.get(ContestModel_.name), "%" + filterString + "%"),
                    cb.like(root.get(ContestModel_.description), "%" + filterString + "%"),
                    cb.like(root.get(ContestModel_.style), "%" + filterString + "%"),
                    cb.like(root.get(ContestModel_.type), "%" + filterString + "%")
                )
            ));
        } else {

            query
                .select(cb.count(root))
                .where(cb.and(
                    cb.or(
                        root.get(ContestModel_.jid).in(contestJids),
                        cb.and(
                            cb.equal(root.get(ContestModel_.scope), ContestScope.PUBLIC.name()),
                            cb.ge(root.get(ContestModel_.endTime), timeNow)
                        )
                    ), cb.or(
                        cb.like(root.get(ContestModel_.name), "%" + filterString + "%"),
                        cb.like(root.get(ContestModel_.description), "%" + filterString + "%"),
                        cb.like(root.get(ContestModel_.style), "%" + filterString + "%"),
                        cb.like(root.get(ContestModel_.type), "%" + filterString + "%")
                    )
                ));
        }
        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestModel> findSortedContestsWithinContestJidsOrIsRunningPublicByFilters(String orderBy, String orderDir, String filterString, Collection<String> contestJids, long offset, long limit, long timeNow) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModel> query = cb.createQuery(getModelClass());
        Root<ContestModel> root = query.from(getModelClass());

        if (contestJids.size() == 0) {
            query.where(cb.and(
                cb.and(
                    cb.equal(root.get(ContestModel_.scope), ContestScope.PUBLIC.name()),
                    cb.ge(root.get(ContestModel_.endTime), timeNow)
                ),
                cb.or(
                    cb.like(root.get(ContestModel_.name), "%" + filterString + "%"),
                    cb.like(root.get(ContestModel_.description), "%" + filterString + "%"),
                    cb.like(root.get(ContestModel_.style), "%" + filterString + "%"),
                    cb.like(root.get(ContestModel_.type), "%" + filterString + "%")
                )
            ));
        } else {

            query
                .where(cb.and(
                    cb.or(
                        root.get(ContestModel_.jid).in(contestJids),
                        cb.and(
                            cb.equal(root.get(ContestModel_.scope), ContestScope.PUBLIC.name()),
                            cb.ge(root.get(ContestModel_.endTime), timeNow)
                        )
                    ), cb.or(
                        cb.like(root.get(ContestModel_.name), "%" + filterString + "%"),
                        cb.like(root.get(ContestModel_.description), "%" + filterString + "%"),
                        cb.like(root.get(ContestModel_.style), "%" + filterString + "%"),
                        cb.like(root.get(ContestModel_.type), "%" + filterString + "%")
                    )
                ));
        }
        return JPA.em().createQuery(query).setFirstResult((int) offset).setMaxResults((int) limit).getResultList();
    }

    @Override
    protected List<SingularAttribute<ContestModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ContestModel_.name, ContestModel_.description, ContestModel_.scope, ContestModel_.style, ContestModel_.type);
    }
}
