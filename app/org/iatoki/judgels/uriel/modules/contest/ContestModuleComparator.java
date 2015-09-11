package org.iatoki.judgels.uriel.modules.contest;

import java.util.Comparator;

public final class ContestModuleComparator implements Comparator<ContestModule> {

    @Override
    public int compare(ContestModule o1, ContestModule o2) {
        return o1.getType().compareTo(o2.getType());
    }
}
