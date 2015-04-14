package org.iatoki.judgels.uriel;

import org.iatoki.judgels.jophiel.commons.AbstractAvatarCacheService;
import org.iatoki.judgels.uriel.models.domains.AvatarCacheModel;

public final class AvatarCacheService extends AbstractAvatarCacheService<AvatarCacheModel> {
    private static final AvatarCacheService INSTANCE = new AvatarCacheService();;

    private AvatarCacheService() {
    }

    public static AvatarCacheService getInstance() {
        return INSTANCE;
    }
}
