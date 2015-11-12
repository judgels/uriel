package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_contestant_organization")
public class ContestContestantOrganizationModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String contestJid;

    public String organization;

}
