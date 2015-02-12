package org.iatoki.judgels.uriel;

import org.iatoki.judgels.sandalphon.commons.AbstractSubmissionUpdaterServiceImpl;
import org.iatoki.judgels.uriel.models.daos.interfaces.ContestSubmissionDao;
import org.iatoki.judgels.uriel.models.domains.ContestSubmissionModel;

public final class SubmissionUpdaterServiceImpl extends AbstractSubmissionUpdaterServiceImpl<ContestSubmissionModel> {
    public SubmissionUpdaterServiceImpl(ContestSubmissionDao submissionDao) {
        super(submissionDao);
    }
}

