<!--
数据添加页面
by shengjing19
create 2025-07-10
last modify 2025-08-27
v1.6.1
-->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.AnimeRecode.model.User" %>
<%@ page import="com.AnimeRecodeConfig.SystemInfo" %>
<%
    // 检查用户是否登录
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    SystemInfo systemInfo = new SystemInfo();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Anime Records DM</title>
    <link rel="stylesheet" href="css/admin.css">
    <link rel="stylesheet" href="fontawesome6.4.0/css/all.css">
</head>

<body>
<!--左侧布局-->
<div class="left-section">
    <div class="sidebar">
        <div class="sidebar-header">
            <h3>Anime Records DM</h3>
            <span><%=systemInfo.getSystemVersion()%></span>
        </div>
        <div class="sidebar-menu">
            <ul>
                <li class="active" data-section="add-anime">
                    <i class="fas fa-plus-circle"></i>
                    <span>动漫添加</span>
                </li>
                <li data-section="anime-list">
                    <i class="fas fa-list"></i>
                    <span>已添加的动漫</span>
                </li>
                <li data-section="anime-list-love">
                    <i class="fas fa-heart"></i>
                    <span>最喜欢的动漫</span>
                </li>
                <li data-section="anime-list-iswatching">
                    <i class="fas fa-running"></i>
                    <span>正在追的动漫</span>
                </li>
            </ul>
        </div>
    </div>
</div>

<!--中间布局-->
<div class="main">
    <div class="header">
        <div class="search">
            <button><i class="fas fa-search"></i></button>
            <input type="text" placeholder="搜索(暂未建设)">
            <i class="fas fa-equals"></i>
        </div>
    </div>

    <!-- 动漫添加区域 -->
    <div id="add-anime-section" class="content-section active">
        <!--添加表单-->
        <form id="animeForm" class="from-main">
            <h2 style="border-bottom: 2px solid rgba(0, 0, 0, 0.05)">动漫添加</h2>
            <div class="form-row">
                <div class="form-group">
                    <label for="animeTitle">动漫名称 *</label>
                    <input type="text" id="animeTitle" name="title" class="form-control" placeholder="请输入动漫名称" required>
                </div>

                <div class="form-group">
                    <label for="episodes">总剧集数 *</label>
                    <input type="number" id="episodes" name="episodes" class="form-control" placeholder="请输入总集数" min="1" required>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="finishDate">看完日期 *</label>
                    <input type="date" id="finishDate" name="finishDate" class="form-control" required>
                </div>

                <div class="form-group">
                    <label>是否最喜欢</label>
                    <div class="checkbox-group">
                        <input type="checkbox" id="isFavorite" name="isFavorite">
                        <label for="isFavorite">标记为最喜欢</label>
                    </div>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>是否正在追番</label>
                    <div class="checkbox-group">
                        <input type="checkbox" id="isWatching" name="isWatching">
                        <label for="isWatching">标记为正在追番</label>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label for="description">动漫描述</label>
                <textarea id="description" name="description" class="form-control" rows="3" placeholder="请输入动漫描述"></textarea>
            </div>

            <div class="form-group">
                <label>动漫封面 *</label>
                <div class="upload-area" id="uploadArea">
                    <div class="upload-icon">
                        <i class="fas fa-cloud-upload-alt"></i>
                    </div>
                    <h3>点击上传封面图片</h3>
                    <p>支持 JPG, PNG 格式，最大 2MB</p>
                    <input type="file" id="coverUpload" name="coverImage" accept="image/*" style="display:none;">
                    <img src="" alt="预览图" class="preview-image" id="previewImage">
                </div>
            </div>

            <div class="form-group">
                <button type="submit" class="btn btn-submit">
                    <i class="fas fa-save"></i> 添加动漫记录
                </button>
            </div>
        </form>
    </div>

    <!-- 动漫列表区域 All -->
    <div id="anime-list-section" class="content-section">
        <div class="admin-anime-list">
            <h2>已添加的动漫</h2>
            <table>
                <thead>
                <tr>
                    <th>封面</th>
                    <th>名称</th>
                    <th>集数</th>
                    <th>看完日期</th>
                    <th>最喜欢</th>
                    <th>正在追</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="animeTableBody">
                     <!-- 数据将通过JS动态加载 -->
                </tbody>
            </table>

            <!-- 分页控件 -->
            <div class="pagination" id="pagination">
                <!-- 分页按钮将通过JS生成 -->
            </div>
        </div>
    </div>

    <!-- 动漫列表区域 最喜欢的动漫 -->
    <div id="anime-list-love-section" class="content-section">
        <div class="admin-anime-list">
            <h2>最喜欢的动漫</h2>
            <table>
                <thead>
                <tr>
                    <th>封面</th>
                    <th>名称</th>
                    <th>集数</th>
                    <th>看完日期</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="animeTableBody-love">
                     <!-- 数据将通过JS动态加载 -->
                </tbody>
            </table>

            <!-- 分页控件 -->
            <div class="pagination" id="pagination-love">
                <!-- 分页按钮将通过JS生成 -->
            </div>
        </div>
    </div>

    <!-- 动漫列表区域 正在追的动漫 -->
    <div id="anime-list-iswatching-section" class="content-section">
        <div class="admin-anime-list">
            <h2>正在追的动漫</h2>
            <table>
                <thead>
                <tr>
                    <th>封面</th>
                    <th>名称</th>
                    <th>集数</th>
                    <th>看完日期</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="animeTableBody-iswatching">
                     <!-- 数据将通过JS动态加载 -->
                </tbody>
            </table>

            <!-- 分页控件 -->
            <div class="pagination" id="pagination-iswatching">
                <!-- 分页按钮将通过JS生成 -->
            </div>
        </div>
    </div>

    <!-- 动漫编辑窗口 -->
    <div class="modal-overlay" id="editModal">
        <div class="modal">
            <div class="modal-content">
                <div class="modal-close" id="closeModal">
                    <i class="fas fa-times"></i>
                </div>

                <h3>编辑动漫信息</h3>

                <form id="editForm">
                    <input type="hidden" id="editId" name="id">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="editTitle">动漫名称 *</label>
                            <input type="text" id="editTitle" name="title" class="form-control" required>
                        </div>

                        <div class="form-group">
                            <label for="editEpisodes">总剧集数 *</label>
                            <input type="number" id="editEpisodes" name="episodes" class="form-control" min="1" required>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="editFinishDate">看完日期 *</label>
                            <input type="date" id="editFinishDate" name="finishDate" class="form-control" required>
                        </div>

                        <div class="form-group">
                            <label>是否最喜欢</label>
                            <div class="checkbox-group">
                                <input type="checkbox" id="editIsFavorite" name="isFavorite">
                                <label for="editIsFavorite">标记为最喜欢</label>
                            </div>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>是否正在追番</label>
                            <div class="checkbox-group">
                                <input type="checkbox" id="editIsWatching" name="isWatching">
                                <label for="editIsWatching">标记为正在追番</label>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="editDescription">动漫描述</label>
                        <textarea id="editDescription" name="description" class="form-control" rows="3"></textarea>
                    </div>

                    <div class="form-group">
                        <label>动漫封面</label>
                        <div class="upload-area" id="editUploadArea">
                            <div class="upload-icon">
                                <i class="fas fa-cloud-upload-alt"></i>
                            </div>
                            <h3>点击上传封面图片</h3>
                            <p>支持 JPG, PNG 格式，最大 2MB</p>
                            <input type="file" id="editCoverUpload" name="coverImage" accept="image/*" style="display:none;">
                            <img src="" alt="预览图" class="preview-image" id="editPreviewImage">
                        </div>
                        <p>如果不修改封面，请留空</p>
                    </div>

                    <div class="form-group">
                        <button type="submit" class="btn btn-submit">
                            <i class="fas fa-save"></i> 保存修改
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!---右侧布局-->
<div class="right-section">
    <div class="profile">
        <div class="info">
            <img src="./img/UsersIcon.png">
            <div class="account">
                <h5><%= user.getUsername() %></h5>
                <p>test@email.com</p>
            </div>
        </div>
        <form action="index.jsp" method="post">
            <button class="btn btn-outline" id="logoutBtn">返回主页</button>
        </form>
    </div>
</div>

<!--版权信息-->
<div class="version-info">
    <span>Anime Records DM <%=systemInfo.getCopyrightInfo()%></span><br>
    <span><%=systemInfo.getAuthorGithub()%></span>
</div>
<script src="js/admin.js"></script>
</body>
</html>