package no.javazone.cake.redux.schedule;

import no.javazone.cake.redux.Configuration;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.*;
import java.util.UUID;

public class EmsDbDao {
    private static volatile EmsDbDao instance = null;
    private final PGPoolingDataSource source;

    private EmsDbDao(PGPoolingDataSource source) {
        this.source = source;
    }

    public static synchronized EmsDbDao get() {
        if (instance != null) {
            return instance;
        }
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("Postgres Data source");
        source.setServerName(Configuration.emsDbServer());
        source.setDatabaseName(Configuration.emsDbName());
        source.setUser(Configuration.emsDbUser());
        source.setPassword(Configuration.emsDbPassword());
        source.setPortNumber(Configuration.emsDbPort());
        source.setMaxConnections(10);
        instance = new EmsDbDao(source);
        return instance;
    }

    public String findOrCreateRoom(String eventid, String roomname) {
        try (Connection connection = source.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("select id from room where eventid = ? and name = ?")) {
                ps.setObject(1,UUID.fromString(eventid));
                ps.setString(2,roomname);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        UUID id = (UUID) rs.getObject(1);
                        return id.toString();
                    }
                }
            }
            UUID id = UUID.randomUUID();
            try (PreparedStatement ps = connection.prepareStatement("insert into room(id,eventid,name) VALUES (?,?,?)")) {
                ps.setObject(1,id);
                ps.setObject(2,UUID.fromString(eventid));
                ps.setString(3,roomname);
                ps.executeUpdate();
            }
            connection.commit();
            return id.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String findOrCreateSlot(String eventid,TalkSlot talkSlot) {
        try (Connection connection = source.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("select id from slot where eventid = ? and start = ? and duration = ?")) {
                ps.setObject(1,UUID.fromString(eventid));
                ps.setTimestamp(2, Timestamp.valueOf(talkSlot.time));
                ps.setInt(3,talkSlot.duration);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        UUID id = (UUID) rs.getObject(1);
                        return id.toString();
                    }
                }
            }
            UUID id = UUID.randomUUID();
            try (PreparedStatement ps = connection.prepareStatement("insert into slot(id,eventid,start,duration) VALUES (?,?,?,?)")) {
                ps.setObject(1,id);
                ps.setObject(2,UUID.fromString(eventid));
                ps.setTimestamp(3,Timestamp.valueOf(talkSlot.time));
                ps.setInt(4,talkSlot.duration);
                ps.executeUpdate();
            }
            connection.commit();
            return id.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRoomForSession(String sessionid,String eventid, String roomid) {
        try (Connection connection = source.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("update session set roomid = ? where id = ? and eventid = ?")) {
                ps.setObject(1,UUID.fromString(roomid));
                ps.setObject(2,UUID.fromString(sessionid));
                ps.setObject(3,UUID.fromString(eventid));
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSlotForSession(String sessionid,String eventid, String slotid) {
        try (Connection connection = source.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("update session set slotid = ? where id = ? and eventid = ?")) {
                ps.setObject(1,UUID.fromString(slotid));
                ps.setObject(2,UUID.fromString(sessionid));
                ps.setObject(3,UUID.fromString(eventid));
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
