/*
 *专门用于存放login.jsp中声明的方法与数据库连接 以达到简化login.jsp的代码清洁
 *by shengjing19
 *create 2025-8-10
 *last modify
 *v1.0
 */
package com.AnimeRecode.Server;

import com.AnimeRecode.dao.UserDAO;
import com.AnimeRecodeConfig.SystemConfig;

import javax.servlet.ServletContext;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginServer {

    private static String filePath_half="/WEB-INF/";

    public String newLoginSQL() throws SQLException {
        int usersCount = 0; //用户数量
        String loginUsername = null;
        SystemConfig systemConfig = SystemConfig.getInstance();
        Connection conn = null;
        try {
            Class.forName(systemConfig.getMysql_jdbc_Driver());
            conn = DriverManager.getConnection(systemConfig.getMysql_jdbc_Url(),systemConfig.getMysql_jdbc_User(), systemConfig.getMysql_jdbc_Pass());
            UserDAO userDAO = new UserDAO(conn);
            usersCount=userDAO.getCount();
            if(usersCount==1)//数据库用户数量为1
            {
                loginUsername=userDAO.getAllUser();//返回登录用户名
                return loginUsername;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        return "登录";
    }

    /*
    * typs_rw  read/write
    * r_w 1/0
    * context
    * filename 文件名
    * */
    public int readANDwrite(String typs_rw ,int r_w,ServletContext context,String filename){
        if(typs_rw.equals("read")){
            return readInstallStatus(context,filename);
        }
        else if(typs_rw.equals("write")){
            writeInstallStatus(context,r_w,filename);
        }
        return 0;
    }

    // 方法：读取安装状态
    private int readInstallStatus(ServletContext context,String filename) {
        try {
            String realPath = context.getRealPath(filePath_half+filename);
            File file = new File(realPath);

            // 如果文件不存在，默认为未安装
            if (!file.exists()) {
                return 0;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String status = reader.readLine();
                if ("1".equals(status)) {
                    return 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 方法：写入安装状态
    private void writeInstallStatus(ServletContext context, int status ,String filename) {
        try {
            String realPath = context.getRealPath(filePath_half+filename);
            File file = new File(realPath);

            // 确保目录存在
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(String.valueOf(status));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

