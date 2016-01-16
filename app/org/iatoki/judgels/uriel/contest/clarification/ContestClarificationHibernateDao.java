package org.iatoki.judgels.uriel.contest.clarification;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJudgelsHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

@Singleton
public final class ContestClarificationHibernateDao extends AbstractJudgelsHibernateDao<ContestClarificationModel> implements ContestClarificationDao {

    public ContestClarificationHibernateDao() {
        super(ContestClarificationModel.class);
    }

    @Override
    public long countUnansweredInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid)), cb.equal(root.get(ContestClarificationModel_.status), ContestClarificationStatus.ASKED.name()));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> getAnsweredJidsInContestAskedByUsers(String contestJid, Collection<String> userJids) {
        if (userJids.isEmpty()) {
            return ImmutableList.of();
        }
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
                .select(root.get(ContestClarificationModel_.jid))
                .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), root.get(ContestClarificationModel_.userCreate).in(userJids), cb.notEqual(root.get(ContestClarificationModel_.status), ContestClarificationStatus.ASKED.name())));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public long countInContestAskedByUsers(String contestJid, Collection<String> userJids) {
        if (userJids.size() == 0) {
            return 0;
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid)), root.get(ContestClarificationModel_.userCreate).in(userJids));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ContestClarificationModel> getAllInContestAskedByUsers(String contestJid, Collection<String> userJids) {
        if (userJids.size() == 0) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestClarificationModel> query = cb.createQuery(ContestClarificationModel.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
                .orderBy(cb.desc(root.get(ContestClarificationModel_.id)))
                .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), root.get(ContestClarificationModel_.userCreate).in(userJids)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getJidsInContestAskedByUsers(String contestJid, Collection<String> userJids) {
        if (userJids.size() == 0) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
              .select(root.get(ContestClarificationModel_.jid))
              .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), root.get(ContestClarificationModel_.userCreate).in(userJids)));

        return JPA.em().createQuery(query).getResultList();
    }
}
