package org.iatoki.judgels.uriel.user;

import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;

@Singleton
public final class UserReadJedisHibernateDao extends AbstractJedisHibernateDao<Long, UserReadModel> implements UserReadDao {

    @Inject
    public UserReadJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, UserReadModel.class);
    }

    @Override
    public boolean existsByUserJidAndTypeAndJid(String userJid, String type, String jid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserReadModel> root = query.from(UserReadModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(UserReadModel_.userJid), userJid), cb.equal(root.get(UserReadModel_.type), type), cb.equal(root.get(UserReadModel_.readJid), jid)));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public long countReadByUserJidAndTypeAndJids(String userJid, String type, Collection<String> jids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserReadModel> root = query.from(UserReadModel.class);

        query
                .select(cb.count(root))
                .where(cb.and(cb.equal(root.get(UserReadModel_.userJid), userJid), cb.equal(root.get(UserReadModel_.type), type), root.get(UserReadModel_.readJid).in(jids)));

        return JPA.em().createQuery(query).getSingleResult();
    }
}
