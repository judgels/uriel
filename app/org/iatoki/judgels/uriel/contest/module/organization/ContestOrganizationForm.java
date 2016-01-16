package org.iatoki.judgels.uriel.contest.module.organization;

import play.data.validation.Constraints;

public class ContestOrganizationForm {

    @Constraints.Required
    public String organization;
}
