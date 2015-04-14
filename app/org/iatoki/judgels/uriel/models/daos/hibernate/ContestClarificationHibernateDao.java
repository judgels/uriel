package org.iatoki.judgels.uriel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.ContestClarificationStatus;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestClarificationDao;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel;
import org.iatoki.judgels.uriel.models.domains.ContestClarificationModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

public final class ContestClarificationHibernateDao extends AbstractHibernateDao<Long, ContestClarificationModel> implements ContestClarificationDao {
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
    public List<Long> findAllAnsweredClarificationIdsInContestByUserJids(String contestJid, Collection<String> userJids) {
        if (userJids.isEmpty()) {
            return ImmutableList.of();
        }
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
                .select(root.get(ContestClarificationModel_.id))
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
                .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), root.get(ContestClarificationModel_.userCreate).in(userJids)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<Long> findClarificationIdsByContestJidAskedByUserJids(String contestJid, Collection<String> userJids) {
        if (userJids.size() == 0) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestClarificationModel> root = query.from(ContestClarificationModel.class);

        query
              .select(root.get(ContestClarificationModel_.id))
              .where(cb.and(cb.equal(root.get(ContestClarificationModel_.contestJid), contestJid), root.get(ContestClarificationModel_.userCreate).in(userJids)));

        return JPA.em().createQuery(query).getResultList();
    }
}
