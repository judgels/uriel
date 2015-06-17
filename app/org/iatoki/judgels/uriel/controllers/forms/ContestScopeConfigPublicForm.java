package org.iatoki.judgels.uriel.controllers.forms;

public class ContestScopeConfigPublicForm {

    public ContestScopeConfigPublicForm() {
    }

    public ContestScopeConfigPublicForm(String registerStartTime, String registerEndTime, long maxRegistrants) {
        this.registerStartTime = registerStartTime;
        this.registerEndTime = registerEndTime;
        this.maxRegistrants = maxRegistrants;
    }

    public String registerStartTime;

    public String registerEndTime;

    public long maxRegistrants;

}
