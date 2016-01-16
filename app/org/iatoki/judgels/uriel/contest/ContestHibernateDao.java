package org.iatoki.judgels.uriel.contest;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJudgelsHibernateDao;

import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class ContestHibernateDao extends AbstractJudgelsHibernateDao<ContestModel> implements ContestDao {

    public ContestHibernateDao() {
        super(ContestModel.class);
    }

    @Override
    protected List<SingularAttribute<ContestModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ContestModel_.name, ContestModel_.description, ContestModel_.style);
    }
}
