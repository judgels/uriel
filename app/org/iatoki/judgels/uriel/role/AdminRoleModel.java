package org.iatoki.judgels.uriel.role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "uriel_role_admin")
public class AdminRoleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    public String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public String createdIp;

    @Column(unique = true, nullable = false)
    public String userJid;
}
