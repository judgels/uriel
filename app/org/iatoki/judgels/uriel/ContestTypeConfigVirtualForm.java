package org.iatoki.judgels.uriel;

public class ContestTypeConfigVirtualForm {

    public ContestTypeConfigVirtualForm() {
    }

    public ContestTypeConfigVirtualForm(long contestDuration, String startTrigger) {
        this.contestDuration = contestDuration;
        this.startTrigger = startTrigger;
    }

    public long contestDuration;

    public String startTrigger;

}
