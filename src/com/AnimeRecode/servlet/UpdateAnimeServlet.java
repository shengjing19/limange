/*
 *动漫数据编辑时数据更新服务
 *通过接口根据请求ID修改相对应ID的动漫数据
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
import com.AnimeRecodeSecurity.idVerify;
import com.AnimeRecodeSecurity.session.loginCheck;
import com.AnimeRecodeSecurity.upload.PicUploadCheck;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/update-anime")
@MultipartConfig
public class UpdateAnimeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //判断是否登录，如果没有登录则禁止使用该接口，防止未授权调用
        User user = loginCheck.checklogin(request,response);
        if (user == null) return;

        request.setCharacterEncoding("UTF-8");

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

        //其他参数获取
        String title = request.getParameter("title");
        int episodes = Integer.parseInt(request.getParameter("episodes"));
        String finishDateStr = request.getParameter("finishDate");
        boolean isFavorite = "on".equals(request.getParameter("isFavorite"));
        boolean isWatching = "on".equals(request.getParameter("isWatching"));
        String description = request.getParameter("description");

        //数据库连接
        Connection conn = null;
        SystemConfig sc = SystemConfig.getInstance();
        try {
            Class.forName(sc.getMysql_jdbc_Driver());
            conn = DriverManager.getConnection(sc.getMysql_jdbc_Url(),sc.getMysql_jdbc_User(),sc.getMysql_jdbc_Pass());
            AnimeDAO animeDAO = new AnimeDAO(conn);

            //根据ID查找动漫
            Anime anime = animeDAO.getAnimeById(id);
            if (anime == null)
            {
                //动漫不存在
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

            // 更新基本信息
            anime.setTitle(title);
            anime.setTotalEpisodes(episodes);
            anime.setFinishDate(finishDate);
            anime.setFavorite(isFavorite);
            anime.setWatching(isWatching);
            anime.setDescription(description);

            // 处理文件上传（如果有新封面）
            Part filePart = request.getPart("coverImage");
            if (filePart != null && filePart.getSize() > 0)
            {
                String fileName = System.currentTimeMillis() + "_" + filePart.getSubmittedFileName();
                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();
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

                anime.setCoverImage("uploads/" + fileName);
            }

            if (animeDAO.updateAnime(anime))
            {
                //200-更新动漫信息成功
                response.setStatus(HttpServletResponse.SC_OK);
            }
            else
            {
                //500服务器错误-更新动漫信息失败
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //500-服务器错误
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
}