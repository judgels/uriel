package org.iatoki.judgels.uriel.contest.contestant.organization;

import org.iatoki.judgels.play.model.AbstractModel;

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
