package org.iatoki.judgels.uriel;

import org.iatoki.judgels.gabriel.commons.AbstractSubmissionServiceImpl;
import org.iatoki.judgels.sealtiel.client.Sealtiel;
import org.iatoki.judgels.uriel.models.daos.interfaces.GradingDao;
import org.iatoki.judgels.uriel.models.daos.interfaces.SubmissionDao;
import org.iatoki.judgels.uriel.models.domains.GradingModel;
import org.iatoki.judgels.uriel.models.domains.SubmissionModel;

public final class SubmissionServiceImpl extends AbstractSubmissionServiceImpl<SubmissionModel, GradingModel> {
    public SubmissionServiceImpl(SubmissionDao submissionDao, GradingDao gradingDao, Sealtiel sealtiel, String gabrielClientJid) {
        super(submissionDao, gradingDao, sealtiel, gabrielClientJid);
    }
}
