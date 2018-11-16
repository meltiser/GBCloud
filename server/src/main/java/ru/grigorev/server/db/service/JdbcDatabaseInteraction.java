package ru.grigorev.server.db.service;

import ru.grigorev.server.db.dao.DAO;
import ru.grigorev.server.db.dao.JdbcDAOimpl;
import ru.grigorev.server.db.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Dmitriy Grigorev
 */
public class JdbcDatabaseInteraction implements DatabaseInteraction {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find Jdbc driver", e);
        }
    }

    private String url;
    private String user;
    private String password;
    private Connection connection;
    private DAO dao;

    public JdbcDatabaseInteraction(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public void initialize() {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
            dao = new JdbcDAOimpl(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create connection to DB", e);
        }
        prepareDatabase();
    }

    private void prepareDatabase() {
        try {
            Statement statement = connection.createStatement();

            createSchemaIfNotExists(statement);
            createTableIfNotExists(statement);
            List<User> users = dao.getAllUsers();
            if (users.isEmpty()) insertExampleUsers();

            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to prepare database", e);
        }
    }

    public void insertExampleUsers() throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute("INSERT INTO \"UsersSchema\".\"Users\" (login, password) VALUES ('Tom', 'tom');");
        statement.execute("INSERT INTO \"UsersSchema\".\"Users\" (login, password) VALUES ('Bob', 'bob');");
        statement.execute("INSERT INTO \"UsersSchema\".\"Users\" (login, password) VALUES ('Nick', 'nick');");
        statement.close();
    }

    private void createSchemaIfNotExists(Statement statement) throws SQLException {
        statement.execute("CREATE SCHEMA IF NOT EXISTS \"UsersSchema\";");
    }

    private void createTableIfNotExists(Statement statement) throws SQLException {
        statement.execute(
                "CREATE TABLE IF NOT EXISTS \"UsersSchema\".\"Users\"\n" +
                        "(\n" +
                        "    id serial NOT NULL,\n" +
                        "    login character(20) NOT NULL UNIQUE,\n" +
                        "    password character(20) NOT NULL,\n" +
                        "    PRIMARY KEY (id)\n" +
                        ")\n" +
                        "WITH (\n" +
                        "    OIDS = FALSE\n" +
                        ");\n" +
                        "\n" +
                        "ALTER TABLE \"UsersSchema\".\"Users\"\n" +
                        "    OWNER to postgres;");
    }

    @Override
    public DAO getDAO() {
        checkInitialize();
        return dao;
    }

    @Override
    public void close() throws Exception {
        checkInitialize();
        connection.close();
    }

    private void checkInitialize() {
        if (connection == null) {
            throw new IllegalStateException("Service must be required");
        }
    }
}
