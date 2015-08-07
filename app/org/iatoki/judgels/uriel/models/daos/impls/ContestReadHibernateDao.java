package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestReadDao;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel;
import org.iatoki.judgels.uriel.models.entities.ContestReadModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestReadDao")
public final class ContestReadHibernateDao extends AbstractHibernateDao<Long, ContestReadModel> implements ContestReadDao {

    public ContestReadHibernateDao() {
        super(ContestReadModel.class);
    }

    @Override
    public boolean existByUserJidAndTypeAndJid(String userJid, String type, String jid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestReadModel> root = query.from(ContestReadModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestReadModel_.userJid), userJid), cb.equal(root.get(ContestReadModel_.type), type), cb.equal(root.get(ContestReadModel_.readJid), jid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public long countReadByUserJidAndTypeAndJidList(String userJid, String type, List<String> jidList) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestReadModel> root = query.from(ContestReadModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestReadModel_.userJid), userJid), cb.equal(root.get(ContestReadModel_.type), type), root.get(ContestReadModel_.readJid).in(jidList)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
