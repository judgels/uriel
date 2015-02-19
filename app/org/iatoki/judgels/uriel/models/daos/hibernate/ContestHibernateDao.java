package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;
import org.iatoki.judgels.uriel.models.domains.ContestModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public final class ContestHibernateDao extends AbstractJudgelsHibernateDao<ContestModel> implements ContestDao {
    public ContestHibernateDao() {
        super(ContestModel.class);
    }

    @Override
    public List<ContestModel> getRunningContests(long timeNow) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModel> query = cb.createQuery(getModelClass());
        Root<ContestModel> root = query.from(getModelClass());

        query
                .where(cb.and(
                        cb.le(root.get(ContestModel_.startTime), timeNow),
                        cb.ge(root.get(ContestModel_.endTime), timeNow)
                ));

        return JPA.em().createQuery(query).getResultList();
    }
}
