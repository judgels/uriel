package org.iatoki.judgels.uriel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.Collection;

@ImplementedBy(UserReadHibernateDao.class)
public interface UserReadDao extends Dao<Long, UserReadModel> {

    boolean existsByUserJidAndTypeAndJid(String userJid, String type, String jid);

    long countReadByUserJidAndTypeAndJids(String userJid, String type, Collection<String> jids);
}
