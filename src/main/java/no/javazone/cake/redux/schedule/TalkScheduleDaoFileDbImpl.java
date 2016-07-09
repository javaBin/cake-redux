package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.Configuration;
import no.javazone.cake.redux.comments.FeedbackDao;
import org.hsqldb.jdbc.JDBCDataSource;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;
import org.jsonbuddy.pojo.JsonGenerator;
import org.jsonbuddy.pojo.PojoMapper;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TalkScheduleDaoFileDbImpl implements TalkSceduleDao {
    private static volatile TalkScheduleDaoFileDbImpl instance = null;

    private final JDBCDataSource jdbcDataSource;

    private TalkScheduleDaoFileDbImpl(JDBCDataSource jdbcDataSource) {
        this.jdbcDataSource = jdbcDataSource;
    }

    public static synchronized TalkSceduleDao get() {
        if (instance != null) {
            return instance;
        }
        String dbFileName = Configuration.scheduleDBFileName();
        //boolean newDB = !new File(dbFileName).exists();

        JDBCDataSource jdbcDataSource = new JDBCDataSource();
        jdbcDataSource.setUrl("jdbc:hsqldb:file:" + dbFileName);
        jdbcDataSource.setUser("SA");
        jdbcDataSource.setPassword("");

        boolean existingDb;
        try (Connection connection = jdbcDataSource.getConnection()) {
            try (ResultSet rs = connection.getMetaData().getTables(null, null, "SCHEDULE", new String[]{"TABLE"})) {
                existingDb = rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (existingDb) {
            instance = new TalkScheduleDaoFileDbImpl(jdbcDataSource);
            return instance;
        }
        try (Connection connection = jdbcDataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE SCHEDULE(" +
                    "talkref varchar(400)," +
                    "data VARCHAR(4000)" +
                    ")")) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        TalkScheduleDaoFileDbImpl talkScheduleDaoFileDb = new TalkScheduleDaoFileDbImpl(jdbcDataSource);



        instance = talkScheduleDaoFileDb;
        return instance;
    }

    @Override
    public void updateSchedule(TalkSchedule talkSchedule) {
        String data = talkSchedule.jsonValue().toJson();
        try (Connection connection = jdbcDataSource.getConnection()) {
            int rows;
            try (PreparedStatement ps = connection.prepareStatement("update SCHEDULE set data = ? where talkref = ?")) {
                ps.setString(1,data);
                ps.setString(2,talkSchedule.talkid);
                rows = ps.executeUpdate();
                if (rows > 0) {
                    connection.commit();
                }
            }
            if (rows == 0) {
                try (PreparedStatement ps = connection.prepareStatement("insert into SCHEDULE(talkref,data) VALUES (?,?)")) {
                    ps.setString(1,talkSchedule.talkid);
                    ps.setString(2,data);
                    ps.executeUpdate();
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<TalkSchedule> getSchedule(String talkid) {
        try (Connection connection = jdbcDataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("select DATA from SCHEDULE where talkref = ?")) {
            ps.setString(1,talkid);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                JsonObject jsonObject = JsonParser.parseToObject(rs.getString(1));
                TalkSchedule talkSchedule = PojoMapper.map(jsonObject, TalkSchedule.class);
                return Optional.of(talkSchedule);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TalkSchedule> allScedules() {
        try (Connection connection = jdbcDataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("select DATA from SCHEDULE");
             ResultSet rs = ps.executeQuery()) {

            List<TalkSchedule> result = new ArrayList<>();
            while (rs.next()) {
                JsonObject jsonObject = JsonParser.parseToObject(rs.getString(1));
                TalkSchedule talkSchedule = PojoMapper.map(jsonObject, TalkSchedule.class);
                result.add(talkSchedule);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
