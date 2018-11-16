package ru.grigorev.server.db.dao;

import ru.grigorev.server.db.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Dmitriy Grigorev
 */
public interface DAO {
    List<User> getAllUsers() throws SQLException;
}
