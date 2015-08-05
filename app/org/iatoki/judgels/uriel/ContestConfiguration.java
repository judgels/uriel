package org.iatoki.judgels.uriel;

public final class ContestConfiguration {

    public ContestConfiguration(long id, String contestJid, String typeConfig, String scopeConfig, String styleConfig) {
        this.id = id;
        this.contestJid = contestJid;
        this.typeConfig = typeConfig;
        this.scopeConfig = scopeConfig;
        this.styleConfig = styleConfig;
    }

    private long id;

    private String contestJid;

    private String typeConfig;

    private String scopeConfig;

    private String styleConfig;

    public long getId() {
        return id;
    }

    public String getContestJid() {
        return contestJid;
    }

    public String getTypeConfig() {
        return typeConfig;
    }

    public String getScopeConfig() {
        return scopeConfig;
    }

    public String getStyleConfig() {
        return styleConfig;
    }
}
