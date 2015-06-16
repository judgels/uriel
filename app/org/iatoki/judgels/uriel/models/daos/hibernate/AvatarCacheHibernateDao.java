package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.jophiel.models.daos.impls.AbstractAvatarCacheHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.AvatarCacheDao;
import org.iatoki.judgels.uriel.models.domains.AvatarCacheModel;

public final class AvatarCacheHibernateDao extends AbstractAvatarCacheHibernateDao<AvatarCacheModel> implements AvatarCacheDao {
    public AvatarCacheHibernateDao() {
        super(AvatarCacheModel.class);
    }

    @Override
    public AvatarCacheModel createAvatarCacheModel() {
        return new AvatarCacheModel();
    }
}
