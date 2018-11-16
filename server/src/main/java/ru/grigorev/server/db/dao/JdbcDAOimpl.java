package ru.grigorev.server.db.dao;

import ru.grigorev.server.db.model.User;

import java.sql.*;
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

    private User convertToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt(1));
        user.setLogin(resultSet.getString(2).trim());
        user.setPassword(resultSet.getString(3).trim());
        return user;
    }
}
