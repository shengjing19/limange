/*
 * 系统参数
 * by shengjing19
 * create 2025-7-2
 * last modify 2025-7-30
 */
package com.AnimeRecodeConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SystemConfig {
    // 单例实例
    private static SystemConfig instance;

    //数据库参数
    private String mysql_jdbc_Driver="com.mysql.cj.jdbc.Driver";
    private String mysql_jdbc_Url;
    private String mysql_jdbc_User;
    private String mysql_jdbc_Pass;

    // 配置配置文件路径
    private static final String CONFIG_FILE = "/WEB-INF/db_config.properties";

    // 私有构造函数，防止外部实例化
    private SystemConfig() {
        // 初始化数据库配置
        initializeDbConfig();
    }

    // 获取单例实例
    public static SystemConfig getInstance() {
        if (instance == null)
        {
            instance = new SystemConfig();
        }
        return instance;
    }

    // 初始化数据库配置
    private void initializeDbConfig() {
        // 尝试从配置文件中读取数据库配置
        try {
            // 使用类加载器获取WEB-INF目录的路径
            String webInfPath = SystemConfig.class.getClassLoader().getResource("").getPath();
            webInfPath = webInfPath.substring(0, webInfPath.indexOf("/WEB-INF/classes") + 1);

            File configFile = new File(webInfPath + CONFIG_FILE);

            if (!configFile.exists()) {
                throw new RuntimeException("数据库配置文件不存在，请先运行安装程序");
            }

            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);

                // 读取配置值
                mysql_jdbc_Url = props.getProperty("db.url");
                mysql_jdbc_User = props.getProperty("db.user");
                mysql_jdbc_Pass = props.getProperty("db.password");

                // 验证所有必需的配置项都存在
                if (mysql_jdbc_Url == null || mysql_jdbc_Url.trim().isEmpty()) {
                    throw new RuntimeException("数据库URL配置缺失");
                }
                if (mysql_jdbc_User == null || mysql_jdbc_User.trim().isEmpty()) {
                    throw new RuntimeException("数据库用户配置缺失");
                }
                if (mysql_jdbc_Pass == null) {
                    throw new RuntimeException("数据库密码配置缺失");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //方法调用
    //数据库参数
    public String getMysql_jdbc_Driver()
    {
        return mysql_jdbc_Driver;
    }

    public String getMysql_jdbc_Url()
    {
        return mysql_jdbc_Url;
    }

    public String getMysql_jdbc_User()
    {
        return mysql_jdbc_User;
    }

    public String getMysql_jdbc_Pass()
    {
        return mysql_jdbc_Pass;
    }

}
