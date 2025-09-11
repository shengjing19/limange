/*
 *动漫删除服务
 *通过接口根据请求ID删除相对应ID的动漫数据
 *by shengjing19
 *create 2025-7-10
 *last modify 2025-7-26
 *v1.2
 */
package com.AnimeRecode.servlet;

import com.AnimeRecode.dao.AnimeDAO;
import com.AnimeRecode.model.Anime;
import com.AnimeRecode.model.User;
import com.AnimeRecodeConfig.SystemConfig;
import com.AnimeRecodeSecurity.idVerify;
import com.AnimeRecodeSecurity.session.loginCheck;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

@WebServlet("/delete-anime")
public class DeleteAnimeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws  IOException {

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
            conn = DriverManager.getConnection(sc.getMysql_jdbc_Url(),sc.getMysql_jdbc_User(), sc.getMysql_jdbc_Pass());
            AnimeDAO animeDAO = new AnimeDAO(conn);

            //查询要删除的动漫图片路径
            Anime delanime = animeDAO.getAnimeById(id);
            String filepath = getServletContext().getRealPath("") +delanime.getCoverImage();
            File file = new File(filepath);

            //删除在本地存储的动漫图片与数据库信息
            if (file.delete()&&animeDAO.deleteAnime(id))
            {
                //200-添加成功
                response.setStatus(HttpServletResponse.SC_OK);
            }
            else
            {
                //500-删除失败
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            //500-删除失败
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
}