package org.iatoki.judgels.uriel.contest.grading.programming;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "uriel_contest_programming_delayed_grading", indexes = {@Index(columnList = "sendAt")})
public class ProgrammingDelayedGradingModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    public String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public String createdIp;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    public String request;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date sendAt;
}
