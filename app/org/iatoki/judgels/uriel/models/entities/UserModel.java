package org.iatoki.judgels.uriel.models.entities;

import org.iatoki.judgels.jophiel.models.entities.AbstractUserModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_user")
public final class UserModel extends AbstractUserModel {

    public String roles;
}
