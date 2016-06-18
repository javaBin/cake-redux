package no.javazone.cake.redux.schedule;

import org.flywaydb.core.Flyway;
import org.hsqldb.jdbc.JDBCDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestHsqlDb {
    public static void main(String[] args) throws Exception {
        JDBCDataSource jdbcDataSource = new JDBCDataSource();
        jdbcDataSource.setUrl("jdbc:hsqldb:mem:testx;shutdown=true");
        jdbcDataSource.setUser("SA");
        jdbcDataSource.setPassword("");

        try (Connection connection = jdbcDataSource.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE something(\n" +
                    "  tekst varchar(20)\n" +
                    ")")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO something(tekst) VALUES ('hoi')")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement("Select tekst from something")) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.println(resultSet.getString(1));
                    }
                }

            }
        }

        System.out.println("done");
    }
}
