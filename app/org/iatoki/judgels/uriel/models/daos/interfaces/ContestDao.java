package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.uriel.models.domains.ContestModel;

import java.util.List;

public interface ContestDao extends JudgelsDao<ContestModel> {

    long countByFilter(String filterString);

    List<ContestModel> findByFilterAndSort(String filterString, String sortBy, String order, long first, long max);
}
