package org.iatoki.judgels.uriel.models.daos.impls;

import org.iatoki.judgels.jophiel.models.daos.impls.AbstractAvatarCacheHibernateDao;
import org.iatoki.judgels.uriel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.uriel.models.entities.AvatarCacheModel;

public final class AvatarCacheHibernateDao extends AbstractAvatarCacheHibernateDao<AvatarCacheModel> implements AvatarCacheDao {
    public AvatarCacheHibernateDao() {
        super(AvatarCacheModel.class);
    }

    @Override
    public AvatarCacheModel createAvatarCacheModel() {
        return new AvatarCacheModel();
    }
}
