package org.iatoki.judgels.uriel.services.impls;

import org.iatoki.judgels.sandalphon.services.ProgrammingSubmissionService;
import org.iatoki.judgels.sandalphon.services.impls.AbstractProgrammingSubmissionServiceImpl;
import org.iatoki.judgels.sealtiel.Sealtiel;
import org.iatoki.judgels.uriel.config.GabrielClientJid;
import org.iatoki.judgels.uriel.models.daos.ProgrammingGradingDao;
import org.iatoki.judgels.uriel.models.daos.ProgrammingSubmissionDao;
import org.iatoki.judgels.uriel.models.entities.ProgrammingGradingModel;
import org.iatoki.judgels.uriel.models.entities.ProgrammingSubmissionModel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("programmingSubmissionService")
public final class ProgrammingSubmissionServiceImpl extends AbstractProgrammingSubmissionServiceImpl<ProgrammingSubmissionModel, ProgrammingGradingModel> implements ProgrammingSubmissionService {

    @Inject
    public ProgrammingSubmissionServiceImpl(ProgrammingSubmissionDao programmingSubmissionDao, ProgrammingGradingDao programmingGradingDao, Sealtiel sealtiel, @GabrielClientJid String gabrielClientJid) {
        super(programmingSubmissionDao, programmingGradingDao, sealtiel, gabrielClientJid);
    }
}
