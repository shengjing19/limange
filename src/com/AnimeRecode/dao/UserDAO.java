/*
 * 处理所有与用户登录数据相关的数据库操作
 * by shengjing19
 * create 2025-7-15
 * last modify 2025-8-10
 * v1.1
 */
package com.AnimeRecode.dao;

import com.AnimeRecode.model.User;
import java.sql.*;

public class UserDAO {
    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    User user = new User();
                    user.setId(resultSet.getInt("uid"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                    return user;
                }
            }
        }
        return null;
    }

    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM users";
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            try (ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    //user.setCount_users(resultSet.getInt("total"));
                    return resultSet.getInt("total");
                }
            }
        }
        return 0;
    }

    public String getAllUser() throws SQLException {
        String sql = "select * from users";
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            try (ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next())
                {
                    return resultSet.getString("username");
                }
            }
        }
        return null;
    }

}