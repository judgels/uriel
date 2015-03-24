package org.iatoki.judgels.uriel;

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
        this.startTime = UrielUtils.convertDateToString(contest.getStartTime());
        this.endTime = UrielUtils.convertDateToString(contest.getEndTime());
        this.clarificationEndTime = UrielUtils.convertDateToString(contest.getClarificationEndTime());
        this.isUsingScoreboard = contest.isUsingScoreboard();
        this.isIncognitoScoreboard = contest.isIncognitoScoreboard();
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

    public boolean isUsingScoreboard;

    public boolean isIncognitoScoreboard;

}
