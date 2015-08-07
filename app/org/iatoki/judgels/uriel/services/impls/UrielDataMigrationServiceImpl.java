package org.iatoki.judgels.uriel.services.impls;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.JidService;
import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;
import org.testng.collections.Maps;
import play.db.jpa.JPA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public final class UrielDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 3;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
        if (databaseVersion < 1) {
            migrateV0toV1();
        }
        if (databaseVersion < 2) {
            migrateV1toV2();
        }
        if (databaseVersion < 3) {
            migrateV2toV3();
        }
    }

    private void migrateV2toV3() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String contestTable = "uriel_contest";
        String contestConfigTable = "uriel_contest_configuration";
        String contestStyleTable = "uriel_contest_style";
        String contestModuleTable = "uriel_contest_module";

        Statement statement = connection.createStatement();
        String contestQuery = "SELECT * FROM " + contestTable + ";";
        ResultSet resultSet = statement.executeQuery(contestQuery);
        while (resultSet.next()) {
            String jid = resultSet.getString("jid");
            String style = resultSet.getString("style");
            String scope = resultSet.getString("scope");
            String type = resultSet.getString("type");
            long startTime = resultSet.getLong("startTime");
            long endTime = resultSet.getLong("endTime");
            long clarificationEndTime = resultSet.getLong("clarificationEndTime");
            boolean isExclusive = resultSet.getBoolean("isExclusive");
            boolean isIncognitoScoreboard = resultSet.getBoolean("isIncognitoScoreboard");
            boolean isUsingScoreboard = resultSet.getBoolean("isUsingScoreboard");
            boolean requiresPassword = resultSet.getBoolean("requiresPassword");

            Statement statement1 = connection.createStatement();
            ResultSet resultSet1 = statement1.executeQuery("SELECT * FROM " + contestConfigTable + " WHERE contestJid = \"" + jid + "\";");
            resultSet1.next();

            String styleConfig = resultSet1.getString("styleConfig");
            String scopeConfig = resultSet1.getString("scopeConfig");
            String typeConfig = resultSet1.getString("typeConfig");
            String userCreate = resultSet1.getString("userCreate");
            long timeCreate = resultSet1.getLong("timeCreate");
            String ipCreate = resultSet1.getString("ipCreate");
            String userUpdate = resultSet1.getString("userUpdate");
            long timeUpdate = resultSet1.getLong("timeUpdate");
            String ipUpdate = resultSet1.getString("ipUpdate");

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + contestStyleTable + "(contestJid, style, config, userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);");
            preparedStatement.setString(1, jid);
            preparedStatement.setString(2, style);
            preparedStatement.setString(3, styleConfig);
            preparedStatement.setString(4, userCreate);
            preparedStatement.setLong(5, timeCreate);
            preparedStatement.setString(6, ipCreate);
            preparedStatement.setString(7, userUpdate);
            preparedStatement.setLong(8, timeUpdate);
            preparedStatement.setString(9, ipUpdate);
            preparedStatement.executeUpdate();

            if (scope.equals("PUBLIC")) {
                Map<String, Long> scopeConfigMap = new Gson().fromJson(scopeConfig, new TypeToken<HashMap<String, Long>>() { }.getType());
                Map<String, Long> newConfigMap = Maps.newHashMap(scopeConfigMap);
                long registerEndTime = newConfigMap.get("registerEndTime");
                newConfigMap.remove("registerEndTime");
                newConfigMap.put("registerDuration", registerEndTime - scopeConfigMap.get("registerStartTime"));

                insertIntoModule(connection, contestModuleTable, jid, "REGISTRATION", new Gson().toJson(scopeConfigMap), userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);
            } else if (scope.equals("PRIVATE")) {
                insertIntoModule(connection, contestModuleTable, jid, "LIMITED", "{}", userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);
            }

            long scoreboardFreezeTime = 0;
            boolean isOfficialScoreboardAllowed = false;
            if (type.equals("STANDARD")) {
                Map<String, Object> typeConfigMap = new Gson().fromJson(typeConfig, new TypeToken<HashMap<String, Object>>() { }.getType());
                scoreboardFreezeTime = (endTime - (long) (double) typeConfigMap.get("scoreboardFreezeTime"));
                isOfficialScoreboardAllowed = (boolean) typeConfigMap.get("isOfficialScoreboardAllowed");
            } else if (type.equals("VIRTUAL")) {
                Map<String, Object> typeConfigMap = new Gson().fromJson(typeConfig, new TypeToken<HashMap<String, Object>>() { }.getType());
                long contestDuration = (long) (double) typeConfigMap.get("contestDuration");
                String startTrigger = (String) typeConfigMap.get("startTrigger");

                Map<String, Long> virtualConfigMap = Maps.newHashMap();
                virtualConfigMap.put("virtualDuration", contestDuration);

                insertIntoModule(connection, contestModuleTable, jid, "VIRTUAL", new Gson().toJson(virtualConfigMap), userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);

                if (startTrigger.equals("COACH")) {
                    insertIntoModule(connection, contestModuleTable, jid, "TEAM", "{}", userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);

                    Map<String, String> triggerConfigMap = Maps.newHashMap();
                    triggerConfigMap.put("contestTrigger", startTrigger);

                    insertIntoModule(connection, contestModuleTable, jid, "TRIGGER", new Gson().toJson(triggerConfigMap), userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);
                }
            }

            if (isExclusive) {
                insertIntoModule(connection, contestModuleTable, jid, "EXCLUSIVE", "{}", userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);
            }

            if (isUsingScoreboard) {
                Map<String, Object> scoreboardConfigMap = Maps.newHashMap();
                scoreboardConfigMap.put("isOfficialScoreboardAllowed", isOfficialScoreboardAllowed);
                scoreboardConfigMap.put("isIncognitoScoreboard", isIncognitoScoreboard);
                scoreboardConfigMap.put("scoreboardFreezeTime", scoreboardFreezeTime);

                insertIntoModule(connection, contestModuleTable, jid, "SCOREBOARD", new Gson().toJson(scoreboardConfigMap), userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);
            }

            if (requiresPassword) {
                insertIntoModule(connection, contestModuleTable, jid, "PASSWORD", "{}", userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);
            }

            Map<String, Object> durationConfigMap = Maps.newHashMap();
            durationConfigMap.put("beginTime", startTime);
            durationConfigMap.put("contestDuration", endTime - startTime);

            insertIntoModule(connection, contestModuleTable, jid, "DURATION", new Gson().toJson(durationConfigMap), userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);

            Map<String, Object> clarificationConfigMap = Maps.newHashMap();
            clarificationConfigMap.put("clarificationDuration", clarificationEndTime - startTime);

            insertIntoModule(connection, contestModuleTable, jid, "CLARIFICATION", new Gson().toJson(clarificationConfigMap), userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);

            insertIntoModule(connection, contestModuleTable, jid, "SUPERVISOR", "{}", userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);

            insertIntoModule(connection, contestModuleTable, jid, "FILE", "{}", userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate);
        }
        statement.execute("ALTER TABLE " + contestTable + " DROP scope;");
        statement.execute("ALTER TABLE " + contestTable + " DROP type;");
        statement.execute("ALTER TABLE " + contestTable + " DROP startTime;");
        statement.execute("ALTER TABLE " + contestTable + " DROP endTime;");
        statement.execute("ALTER TABLE " + contestTable + " DROP clarificationEndTime;");
        statement.execute("ALTER TABLE " + contestTable + " DROP isIncognitoScoreboard;");
        statement.execute("ALTER TABLE " + contestTable + " DROP isExclusive;");
        statement.execute("ALTER TABLE " + contestTable + " DROP isUsingScoreboard;");
        statement.execute("ALTER TABLE " + contestTable + " DROP requiresPassword;");

        statement.execute("DROP TABLE " + contestConfigTable + ";");
    }

    private void insertIntoModule(Connection connection, String contestModuleTable, String contestJid, String moduleName, String moduleConfig, String userCreate, long timeCreate, String ipCreate, String userUpdate, long timeUpdate, String ipUpdate) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + contestModuleTable + "(contestJid, name, config, enabled, userCreate, timeCreate, ipCreate, userUpdate, timeUpdate, ipUpdate) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        preparedStatement.setString(1, contestJid);
        preparedStatement.setString(2, moduleName);
        preparedStatement.setString(3, moduleConfig);
        preparedStatement.setBoolean(4, true);
        preparedStatement.setString(5, userCreate);
        preparedStatement.setLong(6, timeCreate);
        preparedStatement.setString(7, ipCreate);
        preparedStatement.setString(8, userUpdate);
        preparedStatement.setLong(9, timeUpdate);
        preparedStatement.setString(10, ipUpdate);
        preparedStatement.executeUpdate();
    }

    private void migrateV1toV2() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String announcementTable = "uriel_contest_announcement";
        String clarificationTable = "uriel_contest_clarification";
        String readTable = "uriel_contest_read";

        Statement statement = connection.createStatement();
        String announcementQuery = "SELECT * FROM " + announcementTable + "";
        ResultSet resultSet = statement.executeQuery(announcementQuery);
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + announcementTable + " SET jid= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, JidService.getInstance().generateNewJid("COAN").toString());
            preparedStatement.executeUpdate();
        }

        String clarificationQuery = "SELECT * FROM " + clarificationTable + "";
        resultSet = statement.executeQuery(clarificationQuery);
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + clarificationTable + " SET jid= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, JidService.getInstance().generateNewJid("COCL").toString());
            preparedStatement.executeUpdate();
        }

        String readQuery = "SELECT * FROM " + readTable + "";
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
            ResultSet resultSet1 = statement1.executeQuery("SELECT jid FROM " + tableName + " WHERE id=" + readId + ";");
            resultSet1.next();
            readJid = resultSet1.getString("jid");
            resultSet1.close();
            statement1.close();

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + readTable + " SET readJid= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, readJid);
            preparedStatement.executeUpdate();
        }
        resultSet.close();

        statement.executeQuery("ALTER TABLE " + readTable + " DROP readId;");

        statement.close();
    }

    private void migrateV0toV1() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String supervisorTable = "uriel_contest_supervisor";

        Statement statement = connection.createStatement();
        String supervisorQuery = "SELECT * FROM " + supervisorTable + ";";
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

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + supervisorTable + " SET permission= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, permission.toString());
            preparedStatement.executeUpdate();
        }

        statement.executeQuery("ALTER TABLE " + supervisorTable + " DROP announcement;");
        statement.executeQuery("ALTER TABLE " + supervisorTable + " DROP clarification;");
        statement.executeQuery("ALTER TABLE " + supervisorTable + " DROP contestant;");
        statement.executeQuery("ALTER TABLE " + supervisorTable + " DROP problem;");
        statement.executeQuery("ALTER TABLE " + supervisorTable + " DROP submission;");
    }
}
