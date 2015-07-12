package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.domains.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_configuration")
public final class ContestConfigurationModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String contestJid;

    @Column(columnDefinition = "TEXT")
    public String typeConfig;

    @Column(columnDefinition = "TEXT")
    public String scopeConfig;

    @Column(columnDefinition = "TEXT")
    public String styleConfig;

}
