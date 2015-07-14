package org.iatoki.judgels.uriel.services.impls;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;
import play.db.jpa.JPA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UrielDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 1;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
        if ((databaseVersion == 0) && (codeDatabaseVersion == 1)) {
            migrateV0toV1();
        }
    }

    private void migrateV0toV1() throws SQLException {
        String query = "SELECT * FROM `uriel_contest_supervisor`;";
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            JsonObject permission = new JsonObject();
            permission.addProperty("isAllowedAll", false);
            JsonArray allowedPermissions = new JsonArray();
            if (resultSet.getBoolean("announcement")) {
                allowedPermissions.add(new JsonPrimitive("ANNOUNCEMENT"));
            }
            if (resultSet.getBoolean("clarification")) {
                allowedPermissions.add(new JsonPrimitive("CLARIFICATION"));
            }
            if (resultSet.getBoolean("contestant")) {
                allowedPermissions.add(new JsonPrimitive("CONTESTANT"));
            }
            if (resultSet.getBoolean("problem")) {
                allowedPermissions.add(new JsonPrimitive("PROBLEM"));
            }
            if (resultSet.getBoolean("submission")) {
                allowedPermissions.add(new JsonPrimitive("SUBMISSION"));
            }
            permission.add("allowedPermissions", allowedPermissions);

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `uriel_contest_supervisor` SET `permission`= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, permission.toString());
            preparedStatement.executeUpdate();
        }
    }
}
