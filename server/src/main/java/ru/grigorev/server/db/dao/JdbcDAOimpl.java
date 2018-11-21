package ru.grigorev.server.db.dao;

import ru.grigorev.server.db.model.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitriy Grigorev
 */
public class JdbcDAOimpl implements DAO {
    private Connection connection;

    public JdbcDAOimpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        String query = "SELECT * FROM \"UsersSchema\".\"Users\"";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(convertToUser(resultSet));
        }
        return users;
    }

    @Override
    public void insertNewUser(User user) throws SQLException {
        String query = String.format("INSERT INTO \"UsersSchema\".\"Users\"" +
                "(login, password) VALUES ('%s', '%s');", user.getLogin(), user.getPassword());
        Statement statement = connection.createStatement();
        statement.execute(query);
    }

    private User convertToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt(1));
        user.setLogin(resultSet.getString(2).trim());
        user.setPassword(resultSet.getString(3).trim());
        return user;
    }
}
