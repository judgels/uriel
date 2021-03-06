package org.iatoki.judgels.uriel.contest.team;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJudgelsJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

@Singleton
public final class ContestTeamJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ContestTeamModel> implements ContestTeamDao {

    @Inject
    public ContestTeamJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestTeamModel.class);
    }

    @Override
    public List<ContestTeamModel> getAllInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamModel> query = cb.createQuery(getModelClass());
        Root<ContestTeamModel> root = query.from(getModelClass());

        query.where(cb.equal(root.get(ContestTeamModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getJidsInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestTeamModel> root = query.from(getModelClass());

        query.select(root.get(ContestTeamModel_.jid)).where(cb.equal(root.get(ContestTeamModel_.contestJid), contestJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getContestJidsByJids(Collection<String> teamJids) {
        if (teamJids.isEmpty()) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestTeamModel> root = query.from(getModelClass());

        query.select(root.get(ContestTeamModel_.contestJid)).where(root.get(ContestTeamModel_.jid).in(teamJids));

        return JPA.em().createQuery(query).getResultList();
    }
}
