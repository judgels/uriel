package org.iatoki.judgels.uriel.contest.team.coach;

import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public final class ContestTeamCoachJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestTeamCoachModel> implements ContestTeamCoachDao {

    @Inject
    public ContestTeamCoachJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestTeamCoachModel.class);
    }

    @Override
    public boolean isUserRegisteredAsCoachInTeams(String userJid, List<String> teamJids) {
        if (teamJids.isEmpty()) {
            return false;
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestTeamCoachModel> root = query.from(getModelClass());

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContestTeamCoachModel_.coachJid), userJid), root.get(ContestTeamCoachModel_.teamJid).in(teamJids)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ContestTeamCoachModel> getAllByTeamJid(String teamJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamCoachModel> query = cb.createQuery(getModelClass());
        Root<ContestTeamCoachModel> root = query.from(getModelClass());

        query.where(cb.equal(root.get(ContestTeamCoachModel_.teamJid), teamJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestTeamCoachModel> getAllInTeamsByJid(String coachJid, List<String> teamJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamCoachModel> query = cb.createQuery(getModelClass());
        Root<ContestTeamCoachModel> root = query.from(getModelClass());

        query.where(cb.and(cb.equal(root.get(ContestTeamCoachModel_.coachJid), coachJid), root.get(ContestTeamCoachModel_.teamJid).in(teamJids)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<String> getTeamJidsByJid(String coachJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestTeamCoachModel> root = query.from(getModelClass());

        query.select(root.get(ContestTeamCoachModel_.teamJid)).where(cb.equal(root.get(ContestTeamCoachModel_.coachJid), coachJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public boolean isUserCoachByUserJidAndTeamJid(String userJid, String teamJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestTeamCoachModel> root = query.from(getModelClass());

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(ContestTeamCoachModel_.coachJid), userJid), cb.equal(root.get(ContestTeamCoachModel_.teamJid), teamJid)));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }
}
