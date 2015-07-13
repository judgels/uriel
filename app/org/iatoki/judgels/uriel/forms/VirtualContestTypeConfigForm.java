package org.iatoki.judgels.uriel.forms;

public class VirtualContestTypeConfigForm {

    public VirtualContestTypeConfigForm() {
    }

    public VirtualContestTypeConfigForm(long contestDuration, String startTrigger) {
        this.contestDuration = contestDuration;
        this.startTrigger = startTrigger;
    }

    public long contestDuration;

    public String startTrigger;

}
