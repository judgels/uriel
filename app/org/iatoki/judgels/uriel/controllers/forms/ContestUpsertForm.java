package org.iatoki.judgels.uriel.controllers.forms;

import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.uriel.Contest;
import play.data.validation.Constraints;

public final class ContestUpsertForm {

    public ContestUpsertForm() {
    }

    public ContestUpsertForm(Contest contest) {
        this.name = contest.getName();
        this.description = contest.getDescription();
        this.type = contest.getType().name();
        this.scope = contest.getScope().name();
        this.style = contest.getStyle().name();
        this.startTime = JudgelsPlayUtils.formatDateTime(contest.getStartTime().getTime());
        this.endTime = JudgelsPlayUtils.formatDateTime(contest.getEndTime().getTime());
        this.clarificationEndTime = JudgelsPlayUtils.formatDateTime(contest.getClarificationEndTime().getTime());
        this.isExclusive = contest.isExclusive();
        this.isUsingScoreboard = contest.isUsingScoreboard();
        this.isIncognitoScoreboard = contest.isIncognitoScoreboard();
        this.requiresPassword = contest.requiresPassword();
    }

    @Constraints.Required
    public String name;

    public String description;

    @Constraints.Required
    public String type;

    @Constraints.Required
    public String scope;

    @Constraints.Required
    public String style;

    @Constraints.Required
    public String startTime;

    @Constraints.Required
    public String endTime;

    @Constraints.Required
    public String clarificationEndTime;

    public boolean isExclusive;

    public boolean isUsingScoreboard;

    public boolean isIncognitoScoreboard;

    public boolean requiresPassword;
}
