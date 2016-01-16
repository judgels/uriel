package org.iatoki.judgels.uriel.contest;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

@ImplementedBy(ContestHibernateDao.class)
public interface ContestDao extends JudgelsDao<ContestModel> {

}
