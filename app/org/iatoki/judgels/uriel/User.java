package org.iatoki.judgels.uriel;

import java.util.List;

public final class User {

    private long id;

    private String userJid;

    private List<String> roles;

    public User(long id, String userJid, List<String> roles) {
        this.id = id;
        this.userJid = userJid;
        this.roles = roles;
    }

    public long getId() {
        return id;
    }

    public String getUserJid() {
        return userJid;
    }

    public List<String> getRoles() {
        return roles;
    }
}