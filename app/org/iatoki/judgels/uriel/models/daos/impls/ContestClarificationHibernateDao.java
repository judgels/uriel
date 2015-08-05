package org.iatoki.judgels.uriel.models.daos.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.models.daos.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.entities.ContestClarificationModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

@Singleton
@Named("contestClarificationDao")
public final class ContestClarificationHibernateDao extends AbstractJudgelsHibernateDao<ContestClarificationModel> implements ContestClarificationDao {

    public ContestClarificationHibernateDao() {
        super(ContestClarificationModel.class);
    }

    @Override
    public long countUnansweredClarificationByContestJid(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid)), cb.equal(root.get(ContestClarificationModel_.status), ContestClarificationStatus.ASKED.name()));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> findAllAnsweredClarificationJidsInContestByUserJids(String contestJid, Collection<String> userJids) {
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
    public long countClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids) {
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
    public List<ContestClarificationModel> findClarificationsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids) {
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
    public List<String> findClarificationJidsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids) {
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
