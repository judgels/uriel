package org.iatoki.judgels.uriel.avatar;

import org.iatoki.judgels.jophiel.models.daos.hibernate.AbstractAvatarCacheHibernateDao;

import javax.inject.Singleton;

@Singleton
public final class AvatarCacheHibernateDao extends AbstractAvatarCacheHibernateDao<AvatarCacheModel> implements AvatarCacheDao {

    public AvatarCacheHibernateDao() {
        super(AvatarCacheModel.class);
    }

    @Override
    public AvatarCacheModel createAvatarCacheModel() {
        return new AvatarCacheModel();
    }
}
