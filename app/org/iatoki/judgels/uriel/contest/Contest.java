package org.iatoki.judgels.uriel.contest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.iatoki.judgels.uriel.contest.module.ContestModule;
import org.iatoki.judgels.uriel.contest.module.ContestModules;
import org.iatoki.judgels.uriel.contest.style.ContestStyle;
import org.iatoki.judgels.uriel.contest.style.ContestStyleConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Contest {

    private final long id;
    private final String jid;
    private final String name;
    private final String description;
    private final boolean locked;
    private final ContestStyle style;
    private final ContestStyleConfig styleConfig;
    private final Map<ContestModules, ContestModule> modules;

    public Contest(long id, String jid, String name, String description, boolean locked, ContestStyle style, ContestStyleConfig styleConfig, Map<ContestModules, ContestModule> modules) {
        this.id = id;
        this.jid = jid;
        this.name = name;
        this.description = description;
        this.locked = locked;
        this.style = style;
        this.styleConfig = styleConfig;
        this.modules = modules;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLocked() {
        return locked;
    }

    public ContestStyle getStyle() {
        return style;
    }

    public ContestStyleConfig getStyleConfig() {
        return styleConfig;
    }

    public boolean isICPC() {
        return style.equals(ContestStyle.ICPC);
    }

    public boolean isIOI() {
        return style.equals(ContestStyle.IOI);
    }

    public Set<ContestModules> getModulesSet() {
        return ImmutableSet.copyOf(modules.keySet());
    }

    public List<ContestModule> getModules() {
        return ImmutableList.copyOf(modules.values());
    }

    public boolean containsModule(ContestModules contestModules) {
        return modules.containsKey(contestModules);
    }

    public ContestModule getModule(ContestModules contestModules) {
        return modules.get(contestModules);
    }
}