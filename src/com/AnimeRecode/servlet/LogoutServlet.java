/*
 * 登出服务
 * by shengjing19
 * create 2025-7-15
 * last modify 2025-7-25
 * v1.1
 */
package com.AnimeRecode.servlet;

import com.AnimeRecode.model.User;
import com.AnimeRecodeSecurity.session.loginCheck;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet(
        name = "LogoutServlet",
        urlPatterns = {"/logout"}
)
public class LogoutServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //判断是否登录，如果没有登录则禁止使用该接口，防止未授权调用
        User user = loginCheck.checklogin(request,response);
        if (user == null) return;

        // 使当前会话无效
        HttpSession session = request.getSession();
        session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 重定向到登录页面
        response.sendRedirect("login.jsp");
    }
}