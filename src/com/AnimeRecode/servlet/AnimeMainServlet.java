/*
 *动漫记录主服务
 *通过接口根据请求参数返回不同类型的动漫数据（已看完/最喜欢/正在追/统计）
 *以JSON格式返回给前端
 *by shengjing19
 *create 2025-7-1
 *last modify 2025-08-30
 *v1.2.1
 */

package com.AnimeRecode.servlet;

import com.AnimeRecode.dao.AnimeDAO;
import com.AnimeRecode.model.Anime;
import com.AnimeRecode.model.User;
import com.AnimeRecodeConfig.SystemConfig;
import com.AnimeRecodeSecurity.session.loginCheck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(
        name = "AnimeMainServlet",
        urlPatterns = {"/anime-main"} //接口地址
)
public class AnimeMainServlet extends HttpServlet {

    // 配置Gson序列化工具，设置日期格式为yyyy-MM-dd
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    /**
     * GET请求处理方法
     * 根据type参数返回对应类型的JSON数据
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        //判断是否登录，如果没有登录则禁止使用该接口，防止未授权调用
        User user = loginCheck.checklogin(request,response);
        if (user == null) return;

        // 获取请求参数和设置响应格式
        String type = request.getParameter("type");// url/anime-main?type=
        response.setContentType("application/json");//设置响应数据类型格式
        response.setCharacterEncoding("UTF-8");//设置编码

        //数据库连接
        Connection connection = null;
        SystemConfig sc = SystemConfig.getInstance();
        try {
            Class.forName(sc.getMysql_jdbc_Driver());
            connection = DriverManager.getConnection(sc.getMysql_jdbc_Url(),sc.getMysql_jdbc_User(),sc.getMysql_jdbc_Pass());
            AnimeDAO animeDAO = new AnimeDAO(connection);

            //创建一个名为result的JSON对象
            JsonObject result = new JsonObject();

            //根据请求类型调用不同处理方法
            switch (type)
            {
                case "finished": //已看完
                    result = getFinishedAnimes(animeDAO);break;
                case "favorite": //最喜欢
                    result = getFavoriteAnimes(animeDAO);break;
                case "watching": //正在追
                    result = getWatchingAnimes(animeDAO);break;
                case "stats":   //统计大屏的一周中每天的观看的统计数据
                    result = getStats(animeDAO);break;
                default:        // 无效类型处理
                    result.addProperty("error", "Invalid tab type");break;
            }

            response.getWriter().write(gson.toJson(result)); //
        }
        catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", "服务器内部错误: " + e.getMessage());
            response.getWriter().write(gson.toJson(error));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            if (connection != null)
            {
                try {
                    connection.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        //doGet结束
    }
//===========================================================[功能性私有方法(核心)]======================================================
    /*
     * 获取所有已看完的动漫数据
     * 以Json方式返回
     */
    private JsonObject getFinishedAnimes(AnimeDAO animeDAO) throws SQLException {
        JsonObject result = new JsonObject();
        List<Anime> allAnimes = animeDAO.getAllAnimes();//创建集合，用于存放从数据库查询到的所有动漫信息
        List<Anime> finishedAnimes = new ArrayList<>();//创建集合，用于存放已看完的动漫
        // 通过遍历所有动漫，过滤出已看完的动漫
        for (Anime anime : allAnimes)
        {
            if (!anime.isWatching())// isWatching 字段：true 表示正在追，false 表示已看完
            {
                finishedAnimes.add(anime);// 将已看完的动漫加入列表
            }
        }
        /*
          按年份和月份分组
          自动按键排序(降序)的嵌套Map结构
          外层Map：<年份字符串, 内层Map>
          内层Map：<月份字符串, 动漫列表>
         */
        Map<String, Map<String, List<Anime>>> groupedAnimes = new TreeMap<>(Collections.reverseOrder());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Anime anime : finishedAnimes)
        {
            Calendar cal = Calendar.getInstance(); // 创建 Calendar 实例（默认当前时间）
            cal.setTime(anime.getFinishDate());    // 将 Anime 的 finishDate 设置到 Calendar 中
            int year = cal.get(Calendar.YEAR);     //提取年份
            int month = cal.get(Calendar.MONTH) + 1; //提取月份 并+1修正月份

            String yearKey = year + "年";
            String monthKey = String.format("%02d", month) + "月"; // 格式化为两位数月份（例如：5 → "05月"）

            //如果没有对应年份分组，初始化年份分组
            //if判断映射中是否包含了关键字yearKey
            if (!groupedAnimes.containsKey(yearKey))
            {
                groupedAnimes.put(yearKey, new TreeMap<>(Collections.reverseOrder()));//如果没有包含，则将年份降序排列后加入到map中
            }

            //如果没有对应年份的月份分组，初始化月份分组
            if (!groupedAnimes.get(yearKey).containsKey(monthKey))
            {
                // 获取当前年份对应的月份映射，然后添加新的月份键值对
                // monthKey: 月份标识（如"01月"）
                // new ArrayList<>(): 初始化一个空的动漫列表，准备存储该月份看完的动漫
                groupedAnimes.get(yearKey).put(monthKey, new ArrayList<>());
            }

            //如果有对应的年份组与相对于年份的月份组，则将动漫添加到此年份月份组的动漫列表中
            groupedAnimes.get(yearKey).get(monthKey).add(anime);
        }

        // 构建JSON响应
        result.addProperty("total", finishedAnimes.size());//向result json添加总数属性 统计所有已看完的动漫数量
        JsonArray yearGroups = new JsonArray();// 创建年份分组数组（用于存储按年月分组的所有动漫数据） 隶属于result

        //遍历外层Map（年份分组）
        //groupedAnimes结构：<年份, <月份, 动漫列表>>   Map<String, List<Anime>>
        for (Map.Entry<String, Map<String, List<Anime>>> yearEntry : groupedAnimes.entrySet())
        {
            //遍历内层Map（月份分组）
            for (Map.Entry<String, List<Anime>> monthEntry : yearEntry.getValue().entrySet())
            {
                //创建单个年月分组JSON对象 隶属于yearGroups
                JsonObject group = new JsonObject();
                group.addProperty("year", yearEntry.getKey());    // 添加年份属性（如："year":"2025年"）
                group.addProperty("month", monthEntry.getKey()); // 添加月份属性（如："month":"07月"）
                group.addProperty("count", monthEntry.getValue().size()); // 添加计数属性 用于统计当前年月下的动漫数量

                //创建一个用于存放所有已看完的动漫信息的json数组 "animes": []  隶属于group
                JsonArray animesArray = new JsonArray();
                for (Anime anime : monthEntry.getValue())
                {
                    //创建单个动漫info的JSON对象 隶属于animesArray
                    JsonObject animeJson = new JsonObject();
                    // 添加动漫属性
                    animeJson.addProperty("id", anime.getId());
                    animeJson.addProperty("title", anime.getTitle());
                    animeJson.addProperty("coverImage", anime.getCoverImage());
                    animeJson.addProperty("totalEpisodes", anime.getTotalEpisodes());
                    animeJson.addProperty("finishDate", dateFormat.format(anime.getFinishDate()));
                    animesArray.add(animeJson);
                }

                group.add("animes", animesArray);

                yearGroups.add(group);
            }
        }

        result.add("groupedAnimes", yearGroups);  //{"total":43,"groupedAnimes":[]}
        return result;
    }

    /*
     * getFavoriteAnimes(AnimeDAO animeDAO)方法
     * 获取所有最喜欢的动漫数据
     * 以Json方式返回
     */
    private JsonObject getFavoriteAnimes(AnimeDAO animeDAO) throws SQLException {
        JsonObject result = new JsonObject();
        List<Anime> allAnimes = animeDAO.getAllAnimes();
        List<Anime> favoriteAnimes = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 过滤最喜欢的动漫
        for (Anime anime : allAnimes)
        {
            if (anime.isFavorite())
            {
                favoriteAnimes.add(anime);
            }
        }

        // 构建JSON响应
        result.addProperty("total", favoriteAnimes.size());//向result json添加total属性
        //创建一个用于存放所有最喜欢动漫信息的json数组 "animes": []
        JsonArray animesArray = new JsonArray();
        for (Anime anime : favoriteAnimes)
        {
            //创建一个用于存放单个动漫信息json对象 {}
            JsonObject animeJson = new JsonObject();
            //向动漫信息json中添加数据
            animeJson.addProperty("id", anime.getId());
            animeJson.addProperty("title", anime.getTitle());
            animeJson.addProperty("coverImage", anime.getCoverImage());
            animeJson.addProperty("totalEpisodes", anime.getTotalEpisodes());
            animeJson.addProperty("finishDate", dateFormat.format(anime.getFinishDate()));
            animeJson.addProperty("isFavorite", anime.isFavorite());
            animesArray.add(animeJson);
        }

        result.add("animes", animesArray);
        return result;
    }

    /*
     * 获取所有正在追的动漫数据
     * 以Json方式返回
     */
    private JsonObject getWatchingAnimes(AnimeDAO animeDAO) throws SQLException {
        JsonObject result = new JsonObject();
        List<Anime> allAnimes = animeDAO.getAllAnimes();
        List<Anime> watchingAnimes = new ArrayList<>();

        // 过滤正在追的动漫
        for (Anime anime : allAnimes)
        {
            if (anime.isWatching())
            {
                watchingAnimes.add(anime);
            }
        }

        // 构建JSON响应
        result.addProperty("total", watchingAnimes.size());
        JsonArray animesArray = new JsonArray();
        for (Anime anime : watchingAnimes)
        {
            JsonObject animeJson = new JsonObject();
            animeJson.addProperty("id", anime.getId());
            animeJson.addProperty("title", anime.getTitle());
            animeJson.addProperty("coverImage", anime.getCoverImage());
            animeJson.addProperty("totalEpisodes", anime.getTotalEpisodes());
            animeJson.addProperty("isWatching", anime.isWatching());
            animesArray.add(animeJson);
        }

        result.add("animes", animesArray);
        return result;
    }

    /**
     * 获取所有一周内观看动漫数据
     * 以Json方式返回
     * 当前未完全开发完成，现在数据获取为静态，动态应从数据库获取
     */
    private JsonObject getStats(AnimeDAO animeDAO) throws SQLException {
        JsonObject result = new JsonObject();
        List<Anime> allAnimes = animeDAO.getAllAnimes();

        int finished = 0, favorite = 0, watching = 0;
        for(Anime anime : allAnimes)
        {
            if(anime.isWatching())
            {
                watching++;
            }
            if(anime.isFavorite())
            {
                favorite++;
            }
            if(!(anime.isWatching()))
            {
                finished++;
            }
        }

        result.addProperty("totalFinished", finished);
        result.addProperty("totalFavorite", favorite);
        result.addProperty("totalWatching", watching);
        return result;
    }
}