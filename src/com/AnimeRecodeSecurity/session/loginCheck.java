/*
 * 登录安全检测
 * 防止未授权使用接口
 * by shengjing19
 * create 2025/07/25
 * last modify
 * v1.0
 * */
package com.AnimeRecodeSecurity.session;

import com.AnimeRecode.model.User;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class loginCheck  {
    public static User checklogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);  // 获取现有会话，不创建新会话
        if (session == null)
        {
            redirectToNoAccess(response);
            return null;
        }

        User user = (User) session.getAttribute("user");
        if (user == null)
        {
            redirectToNoAccess(response);
            return null;
        }
        return user;
    }

    private static void redirectToNoAccess(HttpServletResponse response) throws IOException {
        response.sendRedirect("noaccess.html");
    }
}
