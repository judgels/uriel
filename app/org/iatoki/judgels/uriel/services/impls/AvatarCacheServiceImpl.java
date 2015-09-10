package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.jophiel.services.impls.AbstractBaseAvatarCacheServiceImpl;
import org.iatoki.judgels.jophiel.models.daos.BaseAvatarCacheDao;
import org.iatoki.judgels.uriel.models.entities.AvatarCacheModel;

public final class AvatarCacheServiceImpl extends AbstractBaseAvatarCacheServiceImpl<AvatarCacheModel> {

    private static AvatarCacheServiceImpl INSTANCE;

    private AvatarCacheServiceImpl(BaseAvatarCacheDao<AvatarCacheModel> avatarCacheDao) {
        super(avatarCacheDao);
    }

    public static synchronized void buildInstance(BaseAvatarCacheDao<AvatarCacheModel> avatarCacheDao) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("AvatarCacheService instance has already been built");
        }
        INSTANCE = new AvatarCacheServiceImpl(avatarCacheDao);
    }

    public static AvatarCacheServiceImpl getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("AvatarCacheService instance has not been built");
        }
        return INSTANCE;
    }
}
