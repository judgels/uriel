package org.iatoki.judgels.uriel.services.impls;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.JidService;
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
        return 2;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
        if (databaseVersion == 0) {
            migrateV0toV1();
            migrateV1toV2();
        } else if (databaseVersion == 1) {
            migrateV1toV2();
        }
    }

    private void migrateV1toV2() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String announcementTable = "uriel_contest_announcement";
        String clarificationTable = "uriel_contest_clarification";
        String readTable = "uriel_contest_read";

        Statement statement = connection.createStatement();
        String announcementQuery = "SELECT * FROM `" +announcementTable+ "`";
        ResultSet resultSet = statement.executeQuery(announcementQuery);
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `" + announcementTable + "` SET `jid`= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, JidService.getInstance().generateNewJid("COAN").toString());
            preparedStatement.executeUpdate();
        }

        String clarificationQuery = "SELECT * FROM `" + clarificationTable + "`";
        resultSet = statement.executeQuery(clarificationQuery);
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `" + clarificationTable + "` SET `jid`= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, JidService.getInstance().generateNewJid("COCL").toString());
            preparedStatement.executeUpdate();
        }

        String readQuery = "SELECT * FROM `" + readTable + "`";
        resultSet = statement.executeQuery(readQuery);
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            long readId = resultSet.getLong("readId");
            String type = resultSet.getString("type");
            String readJid = "";
            String tableName = "";
            if (type.equals("ANNOUNCEMENT")) {
                tableName = announcementTable;
            } else if (type.equals("CLARIFICATION")) {
                tableName = clarificationTable;
            }
            Statement statement1 = connection.createStatement();
            ResultSet resultSet1 = statement1.executeQuery("SELECT `jid` FROM `" + tableName + "` WHERE id=" + readId + ";");
            resultSet1.next();
            readJid = resultSet1.getString("jid");
            resultSet1.close();
            statement1.close();

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `" + readTable + "` SET `readJid`= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, readJid);
            preparedStatement.executeUpdate();
        }
        resultSet.close();

        statement.executeQuery("ALTER TABLE `" + readTable + "` DROP `readId`;");

        statement.close();
    }

    private void migrateV0toV1() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String supervisorTable = "uriel_contest_supervisor";

        Statement statement = connection.createStatement();
        String supervisorQuery = "SELECT * FROM `" + supervisorTable + "`;";
        ResultSet resultSet = statement.executeQuery(supervisorQuery);
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

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `" + supervisorTable + "` SET `permission`= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, permission.toString());
            preparedStatement.executeUpdate();
        }

        statement.executeQuery("ALTER TABLE `" + supervisorTable + "` DROP `announcement`;");
        statement.executeQuery("ALTER TABLE `" + supervisorTable + "` DROP `clarification`;");
        statement.executeQuery("ALTER TABLE `" + supervisorTable + "` DROP `contestant`;");
        statement.executeQuery("ALTER TABLE `" + supervisorTable + "` DROP `problem`;");
        statement.executeQuery("ALTER TABLE `" + supervisorTable + "` DROP `submission`;");
    }
}
