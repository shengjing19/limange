/*
 *管理页面动漫信息获取
 *通过接口根据请求的类型获取相应动漫数据
 *by shengjing19
 *create 2025-8-20
 *last modify
 *v1.0
 */

package com.AnimeRecode.servlet;

import com.AnimeRecode.dao.AnimeDAO;
import com.AnimeRecode.model.Anime;
import com.AnimeRecode.model.User;
import com.AnimeRecodeConfig.SystemConfig;
import com.AnimeRecodeSecurity.session.loginCheck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin-anime-list")
public class AdminAnimeListServlet extends HttpServlet {

    // 配置Gson序列化工具，设置日期格式为yyyy-MM-dd
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 判断是否登录
        User user = loginCheck.checklogin(request, response);
        if (user == null) return;

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String type = request.getParameter("type");
        int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
        int limit = 5; // 每页显示5条记录
        int offset = (page - 1) * limit;

        Connection conn = null;
        SystemConfig sc = SystemConfig.getInstance();
        try {
            Class.forName(sc.getMysql_jdbc_Driver());
            conn = DriverManager.getConnection(sc.getMysql_jdbc_Url(), sc.getMysql_jdbc_User(), sc.getMysql_jdbc_Pass());
            AnimeDAO animeDAO = new AnimeDAO(conn);

            List<Anime> animes;
            int totalCount;

            if ("favorite".equals(type))
            {
                animes = animeDAO.getFavoriteAnimes(limit, offset);
                totalCount = animeDAO.getFavoriteAnimeCount();
            }
            else if ("watching".equals(type))
            {
                animes = animeDAO.getWatchingAnimes(limit, offset);
                totalCount = animeDAO.getWatchingAnimeCount();
            }
            else
            {
                animes = animeDAO.getAnimes(limit, offset);
                totalCount = animeDAO.getAnimeCount();
            }

            //计算一共需要的页面数量
            int totalPages = (int) Math.ceil((double) totalCount / limit);

            Map<String, Object> result = new HashMap<>();
            result.put("animes", animes);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("totalCount", totalCount);

            PrintWriter out = response.getWriter();
            out.print(gson.toJson(result));
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "服务器错误");
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
}