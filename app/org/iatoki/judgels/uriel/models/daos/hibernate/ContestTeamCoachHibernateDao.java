package org.iatoki.judgels.uriel.models.daos.hibernate;

import org.iatoki.judgels.commons.models.daos.hibernate.AbstractHibernateDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestTeamMemberDao;
import org.iatoki.judgels.uriel.models.domains.ContestTeamMemberModel;

public final class ContestTeamCoachHibernateDao extends AbstractHibernateDao<Long, ContestTeamMemberModel> implements ContestTeamMemberDao {
    public ContestTeamCoachHibernateDao() {
        super(ContestTeamMemberModel.class);
    }

}
