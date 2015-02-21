package org.iatoki.judgels.uriel.commons;

import java.util.List;

public class ContestConfig {
    private final List<String> problemJids;
    private final List<String> problemAliases;
    private final List<String> contestantJids;

    public ContestConfig(List<String> problemJids, List<String> problemAliases, List<String> contestantJids) {
        this.problemJids = problemJids;
        this.problemAliases = problemAliases;
        this.contestantJids = contestantJids;
    }

    public List<String> getProblemJids() {
        return problemJids;
    }

    public List<String> getProblemAliases() {
        return problemAliases;
    }

    public List<String> getContestantJids() {
        return contestantJids;
    }
}
