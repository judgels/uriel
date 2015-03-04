package org.iatoki.judgels.uriel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;
import org.iatoki.judgels.uriel.models.domains.ContestModel_;
import play.db.jpa.JPA;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.List;

public final class ContestHibernateDao extends AbstractJudgelsHibernateDao<ContestModel> implements ContestDao {

    private static final String contestJidsForAdmin = "(SELECT jid FROM uriel_contest)";

    private static final String publicContestJids = "(SELECT jid FROM uriel_contest WHERE scope = ?1)";
    private static final String privateContestJidsForManager = "(SELECT jid FROM uriel_contest c JOIN uriel_contest_manager cm ON c.jid=cm.contestJid WHERE c.scope = ?2 AND cm.userJid = ?3)";
    private static final String privateContestJidsForSupervisor = "(SELECT jid FROM uriel_contest c JOIN uriel_contest_permission cs ON c.jid=cs.contestJid WHERE c.scope = ?4 AND cs.userJid = ?5)";
    private static final String privateContestJidsForContestant = "(SELECT jid FROM uriel_contest c JOIN uriel_contest_contestant cc ON c.jid=cc.contestJid WHERE c.scope = ?6 AND cc.userJid = ?7)";

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

    @Override
    public long countAllowedContests(String filterSting, String userJid, boolean isAdmin) {
        String nativeQuery = "SELECT COUNT(*) FROM ";
        Query query;

        if (isAdmin) {
            nativeQuery += "(" + contestJidsForAdmin + ") AS J";
            query = JPA.em().createNativeQuery(nativeQuery);
        } else {
            nativeQuery += "(" + publicContestJids + " UNION " + privateContestJidsForManager + " UNION " + privateContestJidsForSupervisor + " UNION " + privateContestJidsForContestant + ") AS J";
            query = JPA.em().createNativeQuery(nativeQuery);

            query.setParameter(1, "PUBLIC");
            query.setParameter(2, "PRIVATE");
            query.setParameter(4, "PRIVATE");
            query.setParameter(6, "PRIVATE");
            query.setParameter(3, userJid);
            query.setParameter(5, userJid);
            query.setParameter(7, userJid);
        }

        System.out.println(query.toString());

        BigInteger result = (BigInteger) query.getSingleResult();
        return result.longValue();
    }

    @Override
    public List<ContestModel> findSortedAllowedContestsByFilters(String orderBy, String orderDir, String filterString, String userJid, boolean isAdmin, long offset, long limit) {
        String nativeQuery = "SELECT jid FROM ";
        Query query;

        if (isAdmin) {
            nativeQuery += "(" + contestJidsForAdmin + ") AS J";
            query = JPA.em().createNativeQuery(nativeQuery);
        } else {
            nativeQuery += "(" + publicContestJids + " UNION " + privateContestJidsForManager + " UNION " + privateContestJidsForSupervisor + " UNION " + privateContestJidsForContestant + ") AS J";
            query = JPA.em().createNativeQuery(nativeQuery);

            query.setParameter(1, "PUBLIC");
            query.setParameter(2, "PRIVATE");
            query.setParameter(4, "PRIVATE");
            query.setParameter(6, "PRIVATE");
            query.setParameter(3, userJid);
            query.setParameter(5, userJid);
            query.setParameter(7, userJid);
        }

        @SuppressWarnings("unchecked")
        List<String> jids = (List<String>) query.getResultList();

        if (jids.isEmpty()) {
            return ImmutableList.of();
        }

        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ContestModel> q = cb.createQuery(getModelClass());
        Root<ContestModel> root = q.from(getModelClass());

        q.where(root.get(ContestModel_.jid).in(jids));

        return JPA.em().createQuery(q).getResultList();
    }
}
