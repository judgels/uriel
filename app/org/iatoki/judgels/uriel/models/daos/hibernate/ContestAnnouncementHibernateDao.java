package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.ContestAnnouncementStatus;
import org.iatoki.judgels.uriel.models.daos.ContestAnnouncementDao;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel;
import org.iatoki.judgels.uriel.models.entities.ContestAnnouncementModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("contestAnnouncementDao")
public final class ContestAnnouncementHibernateDao extends AbstractJudgelsHibernateDao<ContestAnnouncementModel> implements ContestAnnouncementDao {

    public ContestAnnouncementHibernateDao() {
        super(ContestAnnouncementModel.class);
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
