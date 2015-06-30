package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.sandalphon.services.impls.AbstractSubmissionServiceImpl;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.iatoki.judgels.uriel.models.daos.GradingDao;
import org.iatoki.judgels.uriel.models.daos.SubmissionDao;
import org.iatoki.judgels.uriel.models.entities.GradingModel;
import org.iatoki.judgels.uriel.models.entities.SubmissionModel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("submissionService")
public final class SubmissionServiceImpl extends AbstractSubmissionServiceImpl<SubmissionModel, GradingModel> {

    @Inject
    public SubmissionServiceImpl(SubmissionDao submissionDao, GradingDao gradingDao, Sealtiel sealtiel, String gabrielClientJid) {
        super(submissionDao, gradingDao, sealtiel, gabrielClientJid);
    }
}
