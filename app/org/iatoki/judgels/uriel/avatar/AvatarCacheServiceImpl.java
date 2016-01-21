package org.iatoki.judgels.uriel.avatar;

import org.iatoki.judgels.jophiel.avatar.AbstractBaseAvatarCacheServiceImpl;
import org.iatoki.judgels.jophiel.avatar.BaseAvatarCacheDao;

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
