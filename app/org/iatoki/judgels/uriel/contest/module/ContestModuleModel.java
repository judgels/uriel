package org.iatoki.judgels.uriel.contest.module;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_module")
public final class ContestModuleModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    public String name;

    @Column(columnDefinition = "TEXT")
    public String config;

    public boolean enabled;
}
