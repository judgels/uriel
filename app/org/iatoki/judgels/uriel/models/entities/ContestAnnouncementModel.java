package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_contest_announcement")
@JidPrefix("COAN")
public final class ContestAnnouncementModel extends AbstractJudgelsModel {

    public String contestJid;

    public String title;

    @Column(columnDefinition = "TEXT")
    public String content;

    public String status;
}
