package org.iatoki.judgels.uriel;

import java.util.List;

public final class UserRole {

    private long id;

    private String userJid;

    private String username;

    private String alias;

    private List<String> roles;

    public UserRole(long id, String userJid, String username, String alias, List<String> roles) {
        this.id = id;
        this.userJid = userJid;
        this.username = username;
        this.alias = alias;
        this.roles = roles;
    }

    public long getId() {
        return id;
    }

    public String getUserJid() {
        return userJid;
    }

    public String getUsername() {
        return username;
    }

    public String getAlias() {
        return alias;
    }

    public List<String> getRoles() {
        return roles;
    }
}
