<!--
主页面
by shengjing19
create 2025-07-01
last modify 2025-08-07
v1.8
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.AnimeRecode.model.User" %>
<%@ page import="com.AnimeRecodeConfig.SystemInfo" %>
<%
    // 检查用户是否登录
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String contextPath = request.getContextPath();
    SystemInfo systemInfo = new SystemInfo();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>动漫记录</title>
    <link rel="stylesheet" href="css/index.css">
    <link rel="stylesheet" href="fontawesome6.4.0/css/all.css">
    <script>
        // 全局上下文路径
        const contextPath = "<%= contextPath %>";
        // 全局统计数据 通过AJAX初始化
        const statsData = {
            totalFinished: 0,
            totalFavorite: 0,
            totalWatching: 0
        };
    </script>
</head>
<body>
<!-- 网站头部 -->
<header>
    <div class="container">
        <div class="header-content">
            <div class="logo">
                <h2 style="color: rgba(147, 112, 219, 0.58)">Anime Records</h2>
            </div>

            <div class="user-actions">
                <!--用户菜单 开始-->
                <div class="user-menu-container">
                    <div class="user-icon">
                        <i class="fas fa-user-circle"></i>
                    </div>
                    <div class="dropdown-menu">

                        <!--用户信息-->
                        <div class="user-info">
                            <div class="user-info-2">
                                <div class="user-avatar">
                                    <i class="fas fa-user"></i>
                                </div>
                                <div class="user-name"><%= user.getUsername()%></div>
                                <div class="user-email">test@email.com</div>
                            </div>
                        </div>

                        <!--菜单-->
                        <ul class="menu-items">
                            <!--
                            <li>
                                <i class="fas fa-user-cog"></i>
                                <span>个人中心</span>
                            </li>-->
                            <li>
                                <i class="fas fa-database"></i>
                                <span>数据管理</span>
                            </li>
                            <li>
                                <i class="fas fa-sign-out-alt"></i>
                                <span>退出登录</span>
                            </li>
                        </ul>
                    </div>
                </div>
                <!--用户菜单  结束-->
            </div>
        </div>
    </div>
</header>

<div class="container">
    <div class="main-layout">
        <!-- 左侧菜单栏 -->
        <div class="sidebar">
            <div class="sidebar-menu">
                <ul>
                    <li class="active" data-tab="finished">
                        <i class="fas fa-check-circle"></i>
                        <span>已看完</span>
                    </li>
                    <li data-tab="favorite">
                        <i class="fas fa-heart"></i>
                        <span>最喜欢</span>
                    </li>
                    <li data-tab="watching">
                        <i class="fas fa-running"></i>
                        <span>正在追</span>
                    </li>
                    <li data-tab="stats">
                        <i class="fas fa-chart-bar"></i>
                        <span>统计大屏</span>
                    </li>
                    <li style="pointer-events: none;color: rgba(147, 112, 219, 0.58)">
                        <!--获取版本信息-->
                        <h3>Anime Records</h3><br><span><%=systemInfo.getSystemVersion()%></span>
                    </li>
                </ul>
            </div>
        </div>

        <!-- 主内容区域 -->
        <div class="main-content">
            <div class="content-section">
                <!-- 内容将通过JS动态加载 -->
            </div>
        </div>
    </div>
</div>


<!-- 弹窗 -->
<div class="modal-overlay" id="animeModal">
    <div class="modal">
        <div class="modal-content">

            <div class="modal-close" id="closeModal">
                <i class="fas fa-times"></i>
            </div>

            <div class="modal-image">
                <img src="" alt="" id="modalCover">
            </div>

            <div class="modal-details">
                <h2 class="modal-title" id="modalTitle"></h2>
                <div class="modal-meta">
                    <div><i class="fas fa-tv"></i> <strong>剧集数:</strong> <span id="modalEpisodes"></span></div>
                    <div><i class="fas fa-calendar-alt"></i> <strong>看完日期:</strong> <span id="modalDate"></span></div>
                    <div><i class="fas fa-star"></i> <strong>状态:</strong> <span id="modalStatus">已看完</span></div>
                    <div><i class="fas fa-heart"></i> <strong>最喜欢:</strong> <span id="modalFavorite"></span></div>
                </div>
                <p><span id="modalDescription"></span></p>
            </div>

        </div>
    </div>
</div>

<div class="version-info">
    <!--获取版权信息-->
    <span>Anime Records <%=systemInfo.getCopyrightInfo()%></span><br>
    <span><%=systemInfo.getAuthorGithub()%></span>
</div>

<script src="<%= contextPath %>/js/index.js"></script>
</body>
</html>