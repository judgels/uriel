package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestManagerDao;
import org.iatoki.judgels.uriel.models.entities.ContestManagerModel;
import org.iatoki.judgels.uriel.models.entities.ContestManagerModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestManagerDao")
public final class ContestManagerHibernateDao extends AbstractHibernateDao<Long, ContestManagerModel> implements ContestManagerDao {

    public ContestManagerHibernateDao() {
        super(ContestManagerModel.class);
    }

    @Override
    public boolean existsInContestByJid(String contestJid, String managerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestManagerModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestManagerModel_.userJid), managerJid), cb.equal(root.get(ContestManagerModel_.contestJid), contestJid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<String> getContestJidsByJid(String managerJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestManagerModel> root = query.from(getModelClass());

        query
            .select(root.get(ContestManagerModel_.contestJid))
            .where(cb.equal(root.get(ContestManagerModel_.userJid), managerJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
