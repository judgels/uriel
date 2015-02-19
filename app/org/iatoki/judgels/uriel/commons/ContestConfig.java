package org.iatoki.judgels.uriel.commons;

import java.util.List;
import java.util.Map;

public class ContestConfig {
    private final Map<String, String> problemAliasesByJid;
    private final List<String> contestantJids;

    public ContestConfig(Map<String, String> problemAliasesByJid, List<String> contestantJids) {
        this.problemAliasesByJid = problemAliasesByJid;
        this.contestantJids = contestantJids;
    }

    public Map<String, String> getProblemAliasesByJid() {
        return problemAliasesByJid;
    }

    public List<String> getContestantJids() {
        return contestantJids;
    }
}
