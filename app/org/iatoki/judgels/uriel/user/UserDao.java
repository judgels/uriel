package org.iatoki.judgels.uriel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.models.daos.Dao;

@ImplementedBy(UserHibernateDao.class)
public interface UserDao extends Dao<Long, UserModel> {

    boolean existsByJid(String userJid);

    UserModel findByJid(String userJid);
}
