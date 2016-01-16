package org.iatoki.judgels.uriel.contest.announcement;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

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
