package org.iatoki.judgels.uriel.models.daos.hibernate;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestContestantPasswordDao;
import org.iatoki.judgels.uriel.models.entities.ContestContestantPasswordModel;
import org.iatoki.judgels.uriel.models.entities.ContestContestantPasswordModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named("contestContestantPasswordDao")
public final class ContestContestantPasswordHibernateDao extends AbstractHibernateDao<Long, ContestContestantPasswordModel> implements ContestContestantPasswordDao {

    public ContestContestantPasswordHibernateDao() {
        super(ContestContestantPasswordModel.class);
    }

    @Override
    public boolean existsInContestByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestContestantPasswordModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestContestantPasswordModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantPasswordModel_.contestantJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult() > 0;
    }


    @Override
    public ContestContestantPasswordModel findInContestByContestantJid(String contestJid, String contestantJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestContestantPasswordModel> query = cb.createQuery(getModelClass());
        Root<ContestContestantPasswordModel> root = query.from(getModelClass());

        query
                .where(cb.and(cb.equal(root.get(ContestContestantPasswordModel_.contestJid), contestJid), cb.equal(root.get(ContestContestantPasswordModel_.contestantJid), contestantJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public Map<String, String> getAllMappedInContestByContestantJids(String contestJid, Collection<String> contestantJids) {
        if (contestantJids.isEmpty()) {
            return ImmutableMap.of();
        } else {
            CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
            CriteriaQuery<ContestContestantPasswordModel> query = cb.createQuery(getModelClass());
            Root<ContestContestantPasswordModel> root = query.from(getModelClass());

            query.where(cb.and(cb.equal(root.get(ContestContestantPasswordModel_.contestJid), contestJid), root.get(ContestContestantPasswordModel_.contestantJid).in(contestantJids)));

            List<ContestContestantPasswordModel> models =  JPA.em().createQuery(query).getResultList();

            return models.stream().collect(Collectors.toMap(m -> m.contestantJid, m -> m.password));
        }
    }
}
