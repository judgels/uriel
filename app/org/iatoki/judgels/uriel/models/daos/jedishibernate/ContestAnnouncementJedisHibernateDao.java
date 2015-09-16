package org.iatoki.judgels.uriel.models.daos.jedishibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.models.daos.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel_;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestAnnouncementDao")
public final class ContestAnnouncementJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ContestAnnouncementModel> implements ContestAnnouncementDao {

    @Inject
    public ContestAnnouncementJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ContestAnnouncementModel.class);
    }

    @Override
    public List<String> getPublishedJidsInContest(String contestJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ContestAnnouncementModel> root = query.from(ContestAnnouncementModel.class);

        query
                .select(root.get(ContestAnnouncementModel_.jid))
                .where(cb.and(cb.equal(root.get(ContestAnnouncementModel_.contestJid), contestJid), cb.equal(root.get(ContestAnnouncementModel_.status), ContestAnnouncementStatus.PUBLISHED.name())));

        return JPA.em().createQuery(query).getResultList();
    }
}
