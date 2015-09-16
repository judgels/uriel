package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import org.iatoki.judgels.uriel.models.daos.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.models.entities.ContestTeamMemberModel;
import org.iatoki.judgels.uriel.models.entities.ContestTeamMemberModel_;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

@Singleton
@Named("contestTeamMemberDao")
public final class ContestTeamMemberJedisHibernateDao extends AbstractJedisHibernateDao<Long, ContestTeamMemberModel> implements ContestTeamMemberDao {

    @Inject
    public ContestTeamMemberJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestTeamMemberModel.class);
    }

    @Override
    public boolean isUserRegisteredAsMemberInAnyTeam(String userJid, List<String> teamJids) {
        if (teamJids.isEmpty()) {
            return false;
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ContestTeamMemberModel_.memberJid), userJid), root.get(ContestTeamMemberModel_.teamJid).in(teamJids)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public List<ContestTeamMemberModel> getAllInTeam(String teamJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(cb.equal(root.get(ContestTeamMemberModel_.teamJid), teamJid));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ContestTeamMemberModel> getAllInTeams(Collection<String> teamJids) {
        if (teamJids.isEmpty()) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(root.get(ContestTeamMemberModel_.teamJid).in(teamJids));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public ContestTeamMemberModel findByJidInAnyTeam(String userJid, Collection<String> teamJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestTeamMemberModel> query = cb.createQuery(ContestTeamMemberModel.class);
        Root<ContestTeamMemberModel> root = query.from(ContestTeamMemberModel.class);

        query.where(cb.and(cb.equal(root.get(ContestTeamMemberModel_.memberJid), userJid), root.get(ContestTeamMemberModel_.teamJid).in(teamJids)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
