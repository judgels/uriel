package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.commons.models.domains.AbstractJudgelsModel_;
import org.iatoki.judgels.commons.models.domains.AbstractModel_;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.domains.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.domains.ContestAnnouncementModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public final class ContestAnnouncementHibernateDao extends AbstractHibernateDao<Long, ContestAnnouncementModel> implements ContestAnnouncementDao {

    @Override
    public List<ContestAnnouncementModel> findPublishedByContestJidOrderedByUpdateTime(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestAnnouncementModel> query = cb.createQuery(ContestAnnouncementModel.class);
        Root<ContestAnnouncementModel> root = query.from(ContestAnnouncementModel.class);

        Order orderBy = cb.desc(root.get(AbstractModel_.timeUpdate));
        query.where(cb.and(cb.equal(root.get(ContestAnnouncementModel_.contestJid), contestJid), cb.equal(root.get(ContestAnnouncementModel_.status), ContestAnnouncementStatus.PUBLISHED.name()))).orderBy(orderBy);

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestAnnouncementModel> findByContestJidFilterAndSort(String contestJid, String filterString, List<String> userJids, String sortBy, String order) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestAnnouncementModel> query = cb.createQuery(ContestAnnouncementModel.class);
        Root<ContestAnnouncementModel> root = query.from(ContestAnnouncementModel.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.like(root.get(ContestAnnouncementModel_.title), "%" + filterString + "%"));
        predicates.add(cb.like(root.get(ContestAnnouncementModel_.status), "%" + filterString + "%"));

        Predicate condition = cb.or(predicates.toArray(new Predicate[predicates.size()]));

        if (userJids.size() > 0) {
            condition = cb.or(condition, root.get(AbstractJudgelsModel_.userCreate).in(userJids), root.get(AbstractJudgelsModel_.userUpdate).in(userJids));
        }

        Order orderBy = null;
        if ("asc".equals(order)) {
            orderBy = cb.asc(root.get(sortBy));
        } else {
            orderBy = cb.desc(root.get(sortBy));
        }

        query
            .where(cb.and(cb.equal(root.get(ContestAnnouncementModel_.contestJid), contestJid), condition))
            .orderBy(orderBy);

        return JPA.em().createQuery(query).getResultList();
    }
}
