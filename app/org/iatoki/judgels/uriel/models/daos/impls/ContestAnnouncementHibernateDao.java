package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.commons.models.domains.AbstractModel_;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.models.daos.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestAnnouncementDao")
public final class ContestAnnouncementHibernateDao extends AbstractHibernateDao<Long, ContestAnnouncementModel> implements ContestAnnouncementDao {

    public ContestAnnouncementHibernateDao() {
        super(ContestAnnouncementModel.class);
    }

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
    public List<Long> findAllPublishedAnnouncementIdInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestAnnouncementModel> root = query.from(ContestAnnouncementModel.class);

        query
                .select(root.get(ContestAnnouncementModel_.id))
                .where(cb.and(cb.equal(root.get(ContestAnnouncementModel_.contestJid), contestJid), cb.equal(root.get(ContestAnnouncementModel_.status), ContestAnnouncementStatus.PUBLISHED.name())));

        return JPA.em().createQuery(query).getResultList();
    }
}
