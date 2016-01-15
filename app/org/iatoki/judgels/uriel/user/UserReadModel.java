package org.iatoki.judgels.uriel.user;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_user_read")
public final class UserReadModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String type;

    public String readJid;
}
