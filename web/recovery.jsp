<%--
恢复页面
by shengjing19
create 2025-9-4
last modify
v1.0
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.AnimeRecode.Server.LoginServer" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.BufferedWriter" %>
<%@ page import="java.io.FileWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="com.AnimeRecodeConfig.SystemInfo" %>
<%
    String contextPath = request.getContextPath();
    SystemInfo systemInfo = new SystemInfo();
    LoginServer loginServer = new LoginServer();

    int InsStatus = loginServer.readANDwrite("read",0,application,"install_status.txt"); //读取welcome_status状态值

    if("POST".equals(request.getMethod())){
        session.setAttribute("db-address",request.getParameter("db-address"));
        session.setAttribute("db-port",request.getParameter("db-port"));
        session.setAttribute("db-username",request.getParameter("db-username"));
        session.setAttribute("db-password",request.getParameter("db-password"));

        String dbhost = (String) session.getAttribute("db-address");
        String dbport = (String) session.getAttribute("db-port");
        String dbuser = (String) session.getAttribute("db-username");
        String dbpass = (String) session.getAttribute("db-password");

        if(loginServer.readANDwrite("read",0,application,"install_status.txt")==1)
        {
            response.setStatus(403);
            return;
        }

        if (dbhost == null || dbport == null || dbuser == null || dbpass == null) {
            response.setStatus(400);
            return;
        }

        // 写入配置文件
        String configContent = "db.url=jdbc:mysql://" + dbhost + ":" + dbport + "/limange\n" +
                "db.user=" + dbuser + "\n" +
                "db.password=" + dbpass + "\n";
        String configPath = session.getServletContext().getRealPath("/WEB-INF/db_config.properties");
        File configFile = new File(configPath);
        try {
            // 确保目录存在
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            // 写入配置文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                writer.write(configContent);
            }

            //发送成功信息
            response.setStatus(200);

            //关闭使用权限以保证安全
            loginServer.readANDwrite("write",1,application,"install_status.txt");
        } catch (IOException e) {
            // 处理错误
            response.setStatus(500);
            response.getWriter().write("文件写入失败: " + e.getMessage());
        }
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>恢复</title>
    <link rel="stylesheet" href="css/recovery.css" />
</head>

<%if(InsStatus==1){%>
<body>
<div main>
    <img src="img/时间机器.png" />
    <p title>恢复</p>
    <p subtitle>动漫记录Web应用程序恢复页面</p>

    <div class="cards">
        <a class="card" >
            <div class="card-main">
                <div><img src="img/sql-error.png" style="width: 15%;height: 15%;"></div>
                <div>
                    <h3>为确保安全，该页面功能已被隐藏</h3>
                    <p subtitle>如果您需要使用该页面功能，请将WEB-INF目录下的install_status.txt中的1置为0<br>保存后刷新该页面</p>
                </div>
            </div>
        </a>
    </div>
</div>

<div class="version-info">
    <!--获取版本信息-->
    <span>Anime Recode <%=systemInfo.getSystemVersion()%></span><br>
    <span>Anime Records Recovery <%=systemInfo.getCopyrightInfo()%></span><br>
    <span><%=systemInfo.getAuthorGithub()%></span>
</div>

</body>
</html>
<%return;}%>

<body>
<div main>
    <img src="img/时间机器.png" />
    <p title>恢复</p>
    <p subtitle>动漫记录Web应用程序恢复页面</p>

    <div class="cards">
        <a class="card" id="database-card">
            <div class="card-main">
                <div><img src="img/sql.png" style="width: 15%;height: 15%;"></div>
                <div>
                    <h3>数据库连接信息更改</h3>
                    <p subtitle>如果您需要更换数据库配置或修正错误的数据库配置，请访问这里</p>
                </div>
            </div>
            <div class="card-footer">
                <span class="try-button">开始设置<span class="arrow">→</span></span>
            </div>
        </a>
        <a class="card" id="password-card">
            <div class="card-main">
                <div><img src="img/password.png" style="width: 15%; height: 15%;"></div>
                <div>
                    <h3>更改用户密码(暂未建设)</h3>
                    <p subtitle>在这里您可以更改您的账户密码</p>
                </div>
            </div>
            <div class="card-footer">
                <span class="try-button">开始设置<span class="arrow">→</span></span>
            </div>
        </a>
    </div>
</div>

<!-- 数据库配置窗口 -->
<div class="modal" id="database-modal" >
    <div class="modal-content">
        <div class="modal-header">
            <p class="modal-title">数据库连接设置<br>提交成功后，将关闭恢复页面权限<br>再次使用请修改配置文件</p>
            <button class="close-button">&times;</button>
        </div>
        <form id="database-form">
            <div class="form-group">
                <label for="db-address">数据库访问地址</label>
                <input type="text" id="db-address" name="db-address" placeholder="例如: localhost 或 192.168.1.100" required>
            </div>
            <div class="form-group">
                <label for="db-port">数据库端口</label>
                <input type="number" id="db-port" name="db-port" placeholder="例如: 3306" value="3306" required>
            </div>
            <div class="form-group">
                <label for="db-username">数据库用户名</label>
                <input type="text" id="db-username" name="db-username" placeholder="输入数据库用户名" required>
            </div>
            <div class="form-group">
                <label for="db-password">数据库密码</label>
                <input type="password" id="db-password" name="db-password" placeholder="输入数据库密码" required>
            </div>
            <button type="submit" class="submit-button">保存设置</button>
        </form>
    </div>
</div>

<!-- 密码修改窗口 -->
<div class="modal" id="password-modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2 class="modal-title">更改用户密码</h2>
            <button class="close-button">&times;</button>
        </div>
        <form id="password-form">
            <div class="form-group">
                <label for="current-password">原始密码</label>
                <input type="password" id="current-password" placeholder="输入当前密码" required>
            </div>
            <div class="form-group">
                <label for="new-password">新密码</label>
                <input type="password" id="new-password" placeholder="输入新密码" required>
            </div>
            <div class="form-group">
                <label for="confirm-password">再次输入新密码</label>
                <input type="password" id="confirm-password" placeholder="再次输入新密码" required>
            </div>
            <button type="submit" class="submit-button">更新密码</button>
        </form>
    </div>
</div>

<div class="version-info">
    <!--获取版本信息-->
    <span>Anime Recode <%=systemInfo.getSystemVersion()%></span><br>
    <span>Anime Records Recovery <%=systemInfo.getCopyrightInfo()%></span><br>
    <span><%=systemInfo.getAuthorGithub()%></span>
</div>

<script src="js/recovery.js"></script>
</body>
</html>