package org.iatoki.judgels.uriel;

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
    }

    public String name;

    public String description;

    public String type;

    public String scope;

    public String style;

    public String startTime;

    public String endTime;

}
