package org.iatoki.judgels.uriel.avatar;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.models.daos.BaseAvatarCacheDao;

@ImplementedBy(AvatarCacheJedisHibernateDao.class)
public interface AvatarCacheDao extends BaseAvatarCacheDao<AvatarCacheModel> {

}
