/*
 * 处理所有与动漫数据相关的数据库操作
 * by shengjing19
 * create 2025-7-1
 * last modify 2025-8-20
 * v1.3
 */

package com.AnimeRecode.dao;

import com.AnimeRecode.model.Anime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnimeDAO
{

    private Connection connection; // 数据库连接对象，用于执行所有SQL操作

    /*
     * 带参数的构造函数
     * 通过依赖注入方式接收数据库连接对象，外部创建并传入的数据库连接，连接创建由外部控制
     * 不在DAO内部创建连接，提高代码复用性
     *生命周期由调用的servlet统一管理与关闭
     */
    public AnimeDAO(Connection connection)
    {
        this.connection = connection;// 将传入的连接赋值给当前DAO实例
    }

    /*
     * 添加动漫
     * 由AnimeServlet.java调用
     */
    public boolean addAnime(Anime anime) throws SQLException
    {
        String sql = "INSERT INTO animes (title, cover_image, total_episodes, finish_date, is_favorite, is_watching, `describe`) VALUES (?, ?, ?, ?, ?, ?, ?)";
        //try-with-resources语句 可以没有catch块
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, anime.getTitle());
            statement.setString(2, anime.getCoverImage());
            statement.setInt(3, anime.getTotalEpisodes());
            statement.setDate(4, new java.sql.Date(anime.getFinishDate().getTime()));
            statement.setBoolean(5, anime.isFavorite());
            statement.setBoolean(6, anime.isWatching());
            statement.setString(7, anime.getDescription());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    /*
     * 获取所有动漫信息
     *以泛型集合List<Anime>形式返回查询数据
     */
    public List<Anime> getAllAnimes() throws SQLException
    {
        List<Anime> animes = new ArrayList<>();//创建List集合
        String sql = "SELECT * FROM animes";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql))
        {
            while (resultSet.next())
            {
                //将从数据库查询的结果通过extractAnimeFromResultSet转换成Anime实例，再赋值给anime
                Anime anime = extractAnimeFromResultSet(resultSet);
                //将anime添加到animes集合中
                animes.add(anime);
            }
        }
        return animes;
    }

    /*
     * 通过指定ID获取动漫信息
     */
    public Anime getAnimeById(int id) throws SQLException {
        String sql = "SELECT * FROM animes WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return extractAnimeFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    /*
     * 获取动漫信息所有最喜欢的动漫信息
     */
    public List<Anime> getloveAnime() throws SQLException{
        List<Anime> loveanimes = new ArrayList<>();//创建泛型集合
        String sql = "SELECT * FROM animes WHERE is_favorite = 1";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            try (ResultSet resultSet = statement.executeQuery())
            {
                while(resultSet.next())
                {
                    Anime anime= extractAnimeFromResultSet(resultSet);
                    loveanimes.add(anime);
                }
            }
        }
        return loveanimes;
    }

    /*
     * 获取所有正在追的动漫信息
     */
    public List<Anime> getiswatchingAnime() throws SQLException{
        List<Anime> iswatchinganimes = new ArrayList<>();
        String sql = "SELECT * FROM animes WHERE is_watching = 1";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            try (ResultSet resultSet = statement.executeQuery())
            {
                while(resultSet.next())
                {
                    Anime anime= extractAnimeFromResultSet(resultSet);
                    iswatchinganimes.add(anime);
                }
            }
        }
        return iswatchinganimes;
    }

    /*
     * 通过指定ID删除动漫信息
     * 下方注释是1.2更新处
     * 删除数据之后，修正表的id自动递增的顺序正确
     * 注意：仅支持删最后一个，删除中间数据不能进行id修正
     */
    public boolean deleteAnime(int id) throws SQLException
    {
        //获取表中最大id
        int maxid=0;
        String maxidsql = "SELECT MAX(id) FROM animes";
        try (Statement stmt = connection.createStatement(); ResultSet resultSet = stmt.executeQuery(maxidsql)){
            if(resultSet.next()){
                maxid = resultSet.getInt(1);
            }
        }

        //执行删除
        String sql = "DELETE FROM animes WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();

            //当最大id等于要删除数据的id 则执行自动递增修正
            if(id==maxid && rowsDeleted > 0)
            {
                String zddz = "ALTER TABLE `limange`.`animes` AUTO_INCREMENT = ?";
                try(PreparedStatement pstmt = connection.prepareStatement(zddz)){
                    pstmt.setInt(1, maxid);  // 使用原最大ID作为新起始值
                    pstmt.executeUpdate(); // 执行修改
                }
            }

            return rowsDeleted > 0;
        }
    }

    /*
     * 更新某个动漫信息
     */
    public boolean updateAnime(Anime anime) throws SQLException {
        String sql = "UPDATE animes SET title=?, cover_image=?, total_episodes=?, finish_date=?, is_favorite=?, is_watching=?, `describe`=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, anime.getTitle());
            statement.setString(2, anime.getCoverImage());
            statement.setInt(3, anime.getTotalEpisodes());
            statement.setDate(4, new java.sql.Date(anime.getFinishDate().getTime()));
            statement.setBoolean(5, anime.isFavorite());
            statement.setBoolean(6, anime.isWatching());
            statement.setString(7, anime.getDescription());
            statement.setInt(8, anime.getId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    //========================================v1.3新增===============================================
    /*
     * 分页获取所有动漫信息
     */
    public List<Anime> getAnimes(int limit, int offset) throws SQLException {
        List<Anime> animes = new ArrayList<>();
        String sql = "SELECT * FROM animes ORDER BY id ASC LIMIT ? OFFSET ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Anime anime = extractAnimeFromResultSet(resultSet);
                    animes.add(anime);
                }
            }
        }
        return animes;
    }

    /*
     * 分页获取最喜欢的动漫信息
     */
    public List<Anime> getFavoriteAnimes(int limit, int offset) throws SQLException {
        List<Anime> animes = new ArrayList<>();
        String sql = "SELECT * FROM animes WHERE is_favorite = 1 ORDER BY id ASC LIMIT ? OFFSET ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Anime anime = extractAnimeFromResultSet(resultSet);
                    animes.add(anime);
                }
            }
        }
        return animes;
    }

    /*
     * 分页获取正在追的动漫信息
     */
    public List<Anime> getWatchingAnimes(int limit, int offset) throws SQLException {
        List<Anime> animes = new ArrayList<>();
        String sql = "SELECT * FROM animes WHERE is_watching = 1 ORDER BY id ASC LIMIT ? OFFSET ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Anime anime = extractAnimeFromResultSet(resultSet);
                    animes.add(anime);
                }
            }
        }
        return animes;
    }

    /*
     * 获取所有动漫的总数
     */
    public int getAnimeCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM animes";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    /*
     * 获取最喜欢的动漫总数
     */
    public int getFavoriteAnimeCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM animes WHERE is_favorite = 1";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }

    /*
     * 获取正在追的动漫总数
     */
    public int getWatchingAnimeCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM animes WHERE is_watching = 1";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }
    //========================================v1.3新增[END]===============================================

    /*
     * 将数据库查询结果（ResultSet）转换为 Java 对象（Anime 实例）。
     * 相当于在数据库表和 Java 对象之间架设桥梁，完成 ORM（对象关系映射）的核心转换工作。
     * */
    private Anime extractAnimeFromResultSet(ResultSet resultSet) throws SQLException {
        Anime anime = new Anime();
        anime.setId(resultSet.getInt("id"));
        anime.setTitle(resultSet.getString("title"));
        anime.setCoverImage(resultSet.getString("cover_image"));
        anime.setTotalEpisodes(resultSet.getInt("total_episodes"));
        anime.setFinishDate(resultSet.getDate("finish_date"));
        anime.setFavorite(resultSet.getBoolean("is_favorite"));
        anime.setWatching(resultSet.getBoolean("is_watching"));
        anime.setDescription(resultSet.getString("describe"));
        return anime;
    }
}