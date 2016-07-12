package no.javazone.cake.redux.util;

import org.joda.time.DateTime;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class EmsDbTester {
    private static PGPoolingDataSource createSource(String password) {
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("Postgres Data source");
        source.setServerName("localhost");
        source.setDatabaseName("ems");
        source.setUser("ems");
        source.setPassword(password);
        source.setPortNumber(1111);
        source.setMaxConnections(10);
        return source;
    }

    private static String readPassword() throws IOException {
        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting. Give password");
        String pw = readPassword();
        PGPoolingDataSource source = createSource(pw);
        System.out.println("Created source");
        try (Connection connection = source.getConnection()) {
            //testRead(connection);
            testCreateSlot(connection);
        }
    }

    private static void testCreateSlot(Connection connection) throws SQLException {
        UUID id = UUID.randomUUID();
        UUID eventid = UUID.fromString("aad84b5a-b527-45d4-b532-c5be1f25c1d0");
        LocalDateTime time = LocalDateTime.of(2016, 9, 6, 11, 15, 0);

        Timestamp timestamp = Timestamp.valueOf(time);
        try (PreparedStatement ps=connection.prepareStatement("insert into slot(id,eventid,start,duration) VALUES (?,?,?,?)")) {
            ps.setObject(1,id);
            ps.setObject(2,eventid);
            ps.setTimestamp(3,timestamp);
            ps.setInt(4,45);
            ps.executeUpdate();
        }
        connection.commit();
    }

    private static void testRead(Connection connection) throws SQLException {
        System.out.println("Got connection");
        try (PreparedStatement ps = connection.prepareStatement("select name from event");
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }
}
