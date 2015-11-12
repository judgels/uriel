package org.iatoki.judgels.uriel.modules.contest.organization;

import play.data.validation.Constraints;

public class ContestOrganizationForm {

    @Constraints.Required
    public String organization;
}
