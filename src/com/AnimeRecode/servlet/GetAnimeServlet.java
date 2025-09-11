/*
 *动漫数据编辑时数据获取服务
 *通过接口根据请求ID修改相对应ID的动漫数据
 *by shengjing19
 *create 2025-7-10
 *last modify 2025-7-26
 *v1.2
 */
package com.AnimeRecode.servlet;

import com.AnimeRecode.dao.AnimeDAO;
import com.AnimeRecode.model.Anime;
import com.AnimeRecodeConfig.SystemConfig;
import com.AnimeRecodeSecurity.idVerify;
import com.AnimeRecodeSecurity.session.loginCheck;
import com.google.gson.Gson;
import com.AnimeRecode.model.User;
import com.google.gson.GsonBuilder;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;

@WebServlet("/get-anime")
public class GetAnimeServlet extends HttpServlet {

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws  IOException {

        //判断是否登录，如果没有登录则禁止使用该接口，防止未授权调用
        User user = loginCheck.checklogin(request,response);
        if (user == null) return;

        //id获取与id合法检验
        int id;
        String idParam = request.getParameter("id");
        idVerify idverify = new idVerify();
        if(idverify.verifyID(idParam))
        {
            id = Integer.parseInt(idParam);
        }
        else
        {
            //返回错误信息400坏请求-id不合法或为空
            response.setStatus(400);
            return;
        }

        //数据库连接
        Connection conn = null;
        SystemConfig sc = SystemConfig.getInstance();
        try {
            Class.forName(sc.getMysql_jdbc_Driver());
            conn = DriverManager.getConnection(sc.getMysql_jdbc_Url(),sc.getMysql_jdbc_User(),sc.getMysql_jdbc_Pass());
            AnimeDAO animeDAO = new AnimeDAO(conn);

            //通过指定ID获取指定数据
            Anime anime = animeDAO.getAnimeById(id);
            if (anime != null)
            {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(anime));
                out.flush();
            }
            else
            {
                //返回错误信息404-未找到动漫信息
                response.sendError(404, "Anime not found");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Server error");
        }
        finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
}