package org.iatoki.judgels.uriel.models.domains;

import org.iatoki.judgels.jophiel.commons.models.domains.AbstractUserModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "uriel_user")
public final class UserModel extends AbstractUserModel {

    public String roles;
}
