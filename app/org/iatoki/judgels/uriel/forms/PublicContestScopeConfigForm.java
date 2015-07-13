package org.iatoki.judgels.uriel.forms;

public class PublicContestScopeConfigForm {

    public PublicContestScopeConfigForm() {
    }

    public PublicContestScopeConfigForm(String registerStartTime, String registerEndTime, long maxRegistrants) {
        this.registerStartTime = registerStartTime;
        this.registerEndTime = registerEndTime;
        this.maxRegistrants = maxRegistrants;
    }

    public String registerStartTime;

    public String registerEndTime;

    public long maxRegistrants;

}
