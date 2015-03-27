package org.iatoki.judgels.uriel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.UserRoleDao;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel;
import org.iatoki.judgels.uriel.models.domains.UserRoleModel_;
import play.db.jpa.JPA;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

public final class UserRoleHibernateDao extends AbstractHibernateDao<Long, UserRoleModel> implements UserRoleDao {

    public UserRoleHibernateDao() {
        super(UserRoleModel.class);
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserRoleModel> root = query.from(UserRoleModel.class);

        query
            .select(cb.count(root))
            .where(cb.equal(root.get(UserRoleModel_.userJid), userJid));

        return (JPA.em().createQuery(query).getSingleResult() != 0);
    }

    @Override
    public UserRoleModel findByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserRoleModel> query = cb.createQuery(UserRoleModel.class);
        Root<UserRoleModel> root = query.from(UserRoleModel.class);

        query.where(cb.equal(root.get(UserRoleModel_.userJid), userJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    protected List<SingularAttribute<UserRoleModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(UserRoleModel_.roles);
    }
}
