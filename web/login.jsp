<!--
用户登录页面与第一次欢迎页面
by shengjing19
create 2025-07-15(登录页面)  2025-08-07(第一次欢迎页面) 2025-08-09(新登录界面)
last modify 2025-07-22(登录页面-已移除) 2025-08-18(第一次欢迎页面) 2025-08-13(新登录页面)
v1.1(登录页面-已移除) v1.1(第一次欢迎页面) v1.1.2(新登录界面)
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.AnimeRecodeConfig.SystemConfig" %>
<%@ page import="com.AnimeRecode.Server.LoginServer" %>
<%@ page import="java.util.Objects" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="com.AnimeRecodeConfig.SystemInfo" %>


<%
    String contextPath = request.getContextPath();
    SystemInfo systemInfo = new SystemInfo();
    LoginServer loginServer = new LoginServer();
    //检测SQL数据库连接信息完整性
    try {
        SystemConfig systemConfig = SystemConfig.getInstance();
    }catch (Exception e) {
        // 如果配置加载失败，重定向到安装页面
        loginServer.readANDwrite("write",0,application,"install_status.txt");
        response.sendRedirect(contextPath + "/recovery.jsp");
        return;
    }

    // 处理按钮点击
    if ("GoAnimeRecode".equals(request.getParameter("action"))) {
        loginServer.readANDwrite("write",1,application,"welcome_status.txt");//写入welcome_status状态值，值1
        // 重定向回登录页面
        response.sendRedirect(contextPath + "/login.jsp");
        return;
    }

    int WelStatus = loginServer.readANDwrite("read",0,application,"welcome_status.txt"); //读取welcome_status状态值

    String loginUsername = null;
    if(WelStatus == 1) {
        try {
            loginUsername = loginServer.newLoginSQL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

%>

<html>
<head>
    <meta charset="UTF-8">
    <title>login</title>
    <link rel="stylesheet" href="<%=contextPath%>/css/login.css">
    <link rel="stylesheet" href="fontawesome6.4.0/css/all.min.css">
    <script>
            // 全局上下文路径
            const contextPath = "<%= contextPath %>";
    </script>
</head>

<!------欢迎使用界面------>
<%
    if (WelStatus == 0) {
%>
<body class="bodyOne">
<!-- 欢迎界面 -->
<div class="welcome-main" id="welcome-page">
    <div class="glass-container">
        <nav>
            <div class="welcome-logo"><a href="#">Anime Recode</a></div>
        </nav>
        <div class="welcome-content">
            <div class="welcome-text">
                <h2>Hey! 👋,您已完成配置<br>欢迎使用动漫记录Web程序</h2>
                <p>
                    使用动漫记录Web程序 可以记录你所看完的动漫 <br>
                    与正在追、最喜爱的动漫。
                </p>
                <a href="<%=contextPath%>/login.jsp?action=GoAnimeRecode">
                <button>让我们开始吧！</button>
                </a>
            </div>
            <div class="welcome-image">
                <img src="./img/welcome.gif">
                <div class="welcome-attrebute">
                    <a>Image by Shengjing19</a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="version-info">
    <!--获取版本信息-->
    <span>Anime Recode <%=systemInfo.getSystemVersion()%></span><br>
    <span><%=systemInfo.getAuthorGithub()%></span>
</div>

<script src="<%=contextPath%>/js/login.js"></script>
</body>
</html>
<%return;}%>

<!---------新登录界面----------->
<body class="bodyTwo">
<div class="background">
    <div class="waterfall-column"></div>
    <div class="waterfall-column"></div>
    <div class="waterfall-column"></div>
    <div class="waterfall-column"></div>
</div>

<img src="./img/animerecode.png">
<div class="newlogin">
    <img src="./img/UsersIcon.png"  class="newico"/>
    <p class="newname"><%=loginUsername%></p>

    <form id="loginForm">
        <%if(Objects.equals(loginUsername, "登录")){%>
        <input type="text" name="username" id="username" class="newpass" placeholder="输入用户名" autofocus> <br>
        <%}else{%>
        <input type="hidden" name="username" id="username" value=<%=loginUsername%> >
        <%}%>
        <input type="password" name="password" placeholder="输入密码" class="newpass" id="password" autofocus />
        <div class="error-msg" id="usernameError"></div>
        <div class="error-msg" id="passwordError"></div>
        <button type="submit" style="display: none">登录</button>
    </form>

    <%if(Objects.equals(loginUsername, "登录")){%>
    <p class="newlogintip" id="newTip">你必须输入用户名与密码才能登录</p>
    <%}else {%>
    <p class="newlogintip" id="newTip">你必须输入密码才能登录</p>
    <%}%>
</div>

<div class="version-info">
    <!--获取版本信息-->
    <span>Anime Recode <%=systemInfo.getSystemVersion()%></span><br>
    <span><%=systemInfo.getAuthorGithub()%></span>
</div>

<script src="<%=contextPath%>/js/login.js"></script>
<script type="module" src="<%=contextPath%>/js/newLoginBackground.js"></script>
</body>
</html>
