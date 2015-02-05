package org.iatoki.judgels.uriel.models.daos.interfaces;

import org.iatoki.judgels.commons.models.daos.interfaces.JudgelsDao;
import org.iatoki.judgels.uriel.models.domains.ContestSubmissionModel;

import java.util.List;

public interface ContestSubmissionDao extends JudgelsDao<ContestSubmissionModel> {
    long countByFilters(String contestJid, String problemJid, String authorJid);

    List<ContestSubmissionModel> findByFilters(String contestJid, String problemJid, String authorJid);
}
