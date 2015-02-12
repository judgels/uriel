package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestManagerDao;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel;
import org.iatoki.judgels.uriel.models.domains.ContestManagerModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public final class ContestManagerHibernateDao extends AbstractHibernateDao<Long, ContestManagerModel> implements ContestManagerDao {

    public ContestManagerHibernateDao() {
        super(ContestManagerModel.class);
    }

    @Override
    public boolean existsByManagerJid(String contestJid, String managerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestManagerModel> root = query.from(ContestManagerModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestManagerModel_.userJid), managerJid), cb.equal(root.get(ContestManagerModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public ContestManagerModel findByManagerJid(String contestJid, String managerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestManagerModel> query = cb.createQuery(ContestManagerModel.class);
        Root<ContestManagerModel> root = query.from(ContestManagerModel.class);

        query
                .where(cb.and(cb.equal(root.get(ContestManagerModel_.userJid), managerJid), cb.equal(root.get(ContestManagerModel_.contestJid), contestJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
