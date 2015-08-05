package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.play.services.impls.AbstractBaseJidCacheServiceImpl;
import org.iatoki.judgels.uriel.models.daos.JidCacheDao;
import org.iatoki.judgels.uriel.models.entities.JidCacheModel;

public final class JidCacheServiceImpl extends AbstractBaseJidCacheServiceImpl<JidCacheModel> {

    private static JidCacheServiceImpl INSTANCE;

    private JidCacheServiceImpl(JidCacheDao jidCacheDao) {
        super(jidCacheDao);
    }

    public static synchronized void buildInstance(JidCacheDao jidCacheDao) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("JidCacheService instance has already been built");
        }
        INSTANCE = new JidCacheServiceImpl(jidCacheDao);
    }

    public static JidCacheServiceImpl getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("JidCacheService instance has not been built");
        }
        return INSTANCE;
    }
}
