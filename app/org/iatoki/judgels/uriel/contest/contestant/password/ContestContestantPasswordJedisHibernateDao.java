package org.iatoki.judgels.uriel.contest.contestant.password;

import com.google.common.collect.ImmutableMap;
import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public final class ContestContestantPasswordJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestContestantPasswordModel> implements ContestContestantPasswordDao {

    @Inject
    public ContestContestantPasswordJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestContestantPasswordModel.class);
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
