/*
 * 登录主服务
 * by shengjing19
 * create 2025-7-15
 * last modify 2025-7-17
 * v1.1
 */
package com.AnimeRecode.servlet;

import com.AnimeRecode.dao.UserDAO;
import com.AnimeRecode.model.User;
import com.AnimeRecodeConfig.SystemConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/login"}
)
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 获取数据库连接
        Connection connection = null;
        SystemConfig sc = SystemConfig.getInstance();
        try {
            Class.forName(sc.getMysql_jdbc_Driver());
            connection = DriverManager.getConnection(sc.getMysql_jdbc_Url(),sc.getMysql_jdbc_User(),sc.getMysql_jdbc_Pass());
            UserDAO userDAO = new UserDAO(connection);

            User user = userDAO.getUserByUsername(username);
            if (user != null && password.equals(user.getPassword()))
            {
                // 登录成功，创建session
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                // 设置session过期时间为20分钟（1200秒）
                //已在web.xml中设置全局session过期
                //session.setMaxInactiveInterval(20 * 60);

                // 返回成功状态码
                response.setStatus(HttpServletResponse.SC_OK);
            }
            else
            {
                // 登录失败
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("用户名或密码错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("服务器错误: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}