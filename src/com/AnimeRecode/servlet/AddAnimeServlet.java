/*
 * 动漫提交主服务
 *负责管理页面的提交动漫信息处理
 *by shengjing19
 *create 2025-7-10
 *last modify 2025-7-25
 *v1.2
 */
package com.AnimeRecode.servlet;

import com.AnimeRecode.dao.AnimeDAO;
import com.AnimeRecode.model.Anime;
import com.AnimeRecode.model.User;
import com.AnimeRecodeConfig.SystemConfig;
import com.AnimeRecodeSecurity.session.loginCheck;
import com.AnimeRecodeSecurity.upload.PicUploadCheck;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(
        name = "AnimeServlet",
        urlPatterns = {"/add-anime"},
        initParams = {
                @WebInitParam(name = "savePath", value = "/uploads")
        }
)
@MultipartConfig
public class AddAnimeServlet extends HttpServlet {
    /*
     * Post请求处理方法
     * 将从网页提交的数据(对某些数据处理后)发送到Anime中，并初始化数据库连接，调用AnimeDAO，由AnimeDAO将Anime的数据发送到数据库
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //判断是否登录，如果没有登录则禁止使用该接口，防止未授权调用
        User user = loginCheck.checklogin(request,response);
        if (user == null) return;

        request.setCharacterEncoding("UTF-8");

        //获取提交的动漫数据
        String title = request.getParameter("title");
        Part filePart = request.getPart("coverImage");
        int episodes = Integer.parseInt(request.getParameter("episodes"));
        String finishDateStr = request.getParameter("finishDate");
        boolean isFavorite = "on".equals(request.getParameter("isFavorite"));
        boolean isWatching = "on".equals(request.getParameter("isWatching"));
        String description = request.getParameter("description");

        // 处理封面图片上传
        String fileName = System.currentTimeMillis() + "_" + filePart.getSubmittedFileName();
        String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists())
        {
            uploadDir.mkdir();
        }
        String filePath = uploadPath + File.separator + fileName;
        filePart.write(filePath);

        //图片上传安全性检测
        PicUploadCheck picCheck = new PicUploadCheck();
        if(!picCheck.checkpic(filePath))
        {
            //删除潜在威胁的文件
            File file = new File(filePath);
            file.delete();
            //返回错误信息403拒绝请求-文件安全性检测不通过
            response.sendError(403);
            return;
        }

        // 日期转换
        Date finishDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            finishDate = sdf.parse(finishDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            //返回错误信息400坏请求-日期格式不正确
            response.sendError(400);
            return;
        }

        // 将所有从HTTP获取的数据提交到Anime中
        Anime anime = new Anime();
        anime.setTitle(title);
        anime.setCoverImage("uploads/" + fileName);
        anime.setTotalEpisodes(episodes);
        anime.setFinishDate(finishDate);
        anime.setFavorite(isFavorite);
        anime.setWatching(isWatching);
        anime.setDescription(description);

        // 处理数据库连接 ，配置AnimeDAO连接数据库的信息，由AnimeDAO将提交到Anime的数据提交到数据库中
        Connection connection = null;
        SystemConfig sc = SystemConfig.getInstance();
        try {
            Class.forName(sc.getMysql_jdbc_Driver());
            connection = DriverManager.getConnection(sc.getMysql_jdbc_Url(),sc.getMysql_jdbc_User(),sc.getMysql_jdbc_Pass());
            AnimeDAO animeDAO = new AnimeDAO(connection);

            //动漫添加
            if (animeDAO.addAnime(anime))
            {
                //200-添加成功
                response.setStatus(HttpServletResponse.SC_OK);
            }
            else
            {
                //500服务器错误-添加动漫失败
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            //500服务器错误
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}