package org.iatoki.judgels.uriel.avatar;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.avatar.BaseAvatarCacheDao;

@ImplementedBy(AvatarCacheHibernateDao.class)
public interface AvatarCacheDao extends BaseAvatarCacheDao<AvatarCacheModel> {

}
