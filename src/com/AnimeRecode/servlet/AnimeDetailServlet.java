/*
 *动漫展开窗口数据服务
 *通过接口根据请求ID返回相对应ID的动漫数据
 *以JSON格式返回给前端
 *by shengjing19
 *create 2025-7-1
 *last modify 2025-7-26
 *v1.1
 */
package com.AnimeRecode.servlet;

import com.AnimeRecode.dao.AnimeDAO;
import com.AnimeRecode.model.Anime;
import com.AnimeRecode.model.User;
import com.AnimeRecodeConfig.SystemConfig;
import com.AnimeRecodeSecurity.idVerify;
import com.AnimeRecodeSecurity.session.loginCheck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;

@WebServlet(
        name = "AnimeDetailServlet",
        urlPatterns = {"/anime-detail"}
)
public class AnimeDetailServlet extends HttpServlet {

    // 配置Gson序列化工具，设置日期格式为yyyy-MM-dd
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    /**
     * GET请求处理方法
     * 根据ID参数返回对应类型的JSON数据
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
            //id不合法或为空
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //设置响应格式信息
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        //数据库连接
        Connection connection = null;
        SystemConfig sc = SystemConfig.getInstance();
        try {
            Class.forName(sc.getMysql_jdbc_Driver());
            connection = DriverManager.getConnection(sc.getMysql_jdbc_Url(),sc.getMysql_jdbc_User(),sc.getMysql_jdbc_Pass());
            AnimeDAO animeDAO = new AnimeDAO(connection);

            //通过ID查找动漫信息
            Anime anime = animeDAO.getAnimeById(id);
            if (anime == null)
            {
                sendError(response, "未找到ID为 " + id + " 的动漫", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 构建JSON响应
            JsonObject animeJson = new JsonObject();
            animeJson.addProperty("id", anime.getId());
            animeJson.addProperty("title", anime.getTitle());
            animeJson.addProperty("coverImage", anime.getCoverImage());
            animeJson.addProperty("totalEpisodes", anime.getTotalEpisodes());
            animeJson.addProperty("finishDate", new SimpleDateFormat("yyyy-MM-dd").format(anime.getFinishDate()));
            animeJson.addProperty("isFavorite", anime.isFavorite());
            animeJson.addProperty("isWatching", anime.isWatching());
            animeJson.addProperty("description",anime.getDescription()); // 实际项目中应从数据库获取

            response.getWriter().write(gson.toJson(animeJson));
        }
        catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器内部错误: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            if (connection != null)
            {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(gson.toJson(error));
    }
}