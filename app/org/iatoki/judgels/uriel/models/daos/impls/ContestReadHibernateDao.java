package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestReadDao;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestReadHibernateDao extends AbstractHibernateDao<Long, ContestReadModel> implements ContestReadDao {

    public ContestReadHibernateDao() {
        super(ContestReadModel.class);
    }

    @Override
    public boolean existByUserJidAndTypeAndId(String userJid, String type, long id) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestReadModel> root = query.from(ContestReadModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestReadModel_.userJid), userJid), cb.equal(root.get(ContestReadModel_.type), type), cb.equal(root.get(ContestReadModel_.readId), id)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public long countReadByUserJidAndTypeAndIdList(String userJid, String type, List<Long> IdList) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestReadModel> root = query.from(ContestReadModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestReadModel_.userJid), userJid), cb.equal(root.get(ContestReadModel_.type), type), root.get(ContestReadModel_.readId).in(IdList)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
