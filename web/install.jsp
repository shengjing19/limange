<%@ page import="java.sql.*, java.io.*, java.util.*" %>
<%@ page import="com.AnimeRecodeConfig.SystemInfo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%!
    /*
    数据库与应用程序初始化页面
    by shengjing19
    create 2025-08-01
    modify 2025-08-07
    v1.1
    该页面完全独立
    (放在这的原因是防止因为使用out.print输出JSON时在out.print之前的注释被写入响应)
    */

    // 方法：更新进度 (在声明块中定义)
    private void updateProgress(javax.servlet.http.HttpSession session, int step, String message) {
        Map<String, Object> progress = (Map<String, Object>) session.getAttribute("install_progress");
        if (progress != null) {
            synchronized(progress) { // 添加同步块确保线程安全
                progress.put("currentStep", step);
                progress.put("message", message);
                @SuppressWarnings("unchecked")
                List<String> messages = (List<String>) progress.get("messages");
                if (messages != null) {
                    messages.add(message);
                }
            }
        }
    }

    // 方法：JSON字符串转义
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // 方法：读取安装状态
    private int readInstallStatus(ServletContext context) {
        try {
            String realPath = context.getRealPath("/WEB-INF/install_status.txt");
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
    private void writeInstallStatus(ServletContext context, int status) {
        try {
            String realPath = context.getRealPath("/WEB-INF/install_status.txt");
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

%>

<%
    SystemInfo systemInfo = new SystemInfo();

    // 处理数据库连接和安装逻辑
    String step = request.getParameter("step") != null ? request.getParameter("step") : "1";

    // 存储用户输入
    if ("POST".equals(request.getMethod())) {
        if ("3".equals(step)) {
            session.setAttribute("db_host", request.getParameter("db_host"));
            session.setAttribute("db_port", request.getParameter("db_port"));
            session.setAttribute("db_user", request.getParameter("db_user"));
            session.setAttribute("db_pass", request.getParameter("db_pass"));
            response.sendRedirect("install.jsp?step=4");
            return;
        } else if ("4".equals(step)) {
            session.setAttribute("admin_user", request.getParameter("admin_user"));
            session.setAttribute("admin_pass", request.getParameter("admin_pass"));
            response.sendRedirect("install.jsp?step=5");
            return;
        }
    }

    // 处理安装请求
    if ("5".equals(step)) {
        //获取从session提交的数据
        //数据库参数
        String dbhost = (String) session.getAttribute("db_host");
        String dbport = (String) session.getAttribute("db_port");
        String dbuser = (String) session.getAttribute("db_user");
        String dbpass = (String) session.getAttribute("db_pass");
        String dburl = "jdbc:mysql://" + dbhost + ":" + dbport + "/";
        //系统参数
        String sysuser = (String) session.getAttribute("admin_user");
        String syspass = (String) session.getAttribute("admin_pass");

        // 添加空值检查
        if (dbhost == null || dbport == null || dbuser == null || dbpass == null) {
            session.setAttribute("install_error", "数据库配置信息不完整");
            response.sendRedirect("install.jsp?step=3");
            return;
        }
        if (sysuser == null || syspass == null) {
            session.setAttribute("install_error", "管理员账户信息不完整");
            response.sendRedirect("install.jsp?step=4");
            return;
        }

        // 创建进度跟踪器
        Map<String, Object> progress = new LinkedHashMap<>();
        progress.put("status", "running");//状态
        progress.put("currentStep", 0);//完成数
        progress.put("totalSteps", 6);//总数
        progress.put("messages", new ArrayList<String>());
        session.setAttribute("install_progress", progress);

        // 创建final的session引用
        final HttpSession finalSession = session;

        //启动安装线程
        new Thread(() -> {
            try {
                Connection conn =null;
                Statement stmt=null;
                try {
                    //连接数据库服务器
                    updateProgress(finalSession, 1, "正在连接数据库服务器(" + dbhost + ":" + dbport + ")...");
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    conn = DriverManager.getConnection(dburl, dbuser, dbpass);
                    stmt = conn.createStatement();

                    //创建数据库
                    updateProgress(finalSession, 2, "创建数据库: limange...");
                    String creatsql = "CREATE DATABASE limange";
                    stmt.executeUpdate(creatsql);

                    //切换到数据库
                    stmt.execute("use limange");

                    //创建主数据表
                    updateProgress(finalSession, 3, "创建动漫记录表...");
                    String createmaintab = "CREATE TABLE `animes`  (" +
                            "  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
                            "  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动漫名称'," +
                            "  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '封面图片路径'," +
                            "  `total_episodes` smallint(5) UNSIGNED NOT NULL COMMENT '总剧集数'," +
                            "  `finish_date` date NOT NULL COMMENT '看完日期'," +
                            "  `is_favorite` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否标记为最喜欢'," +
                            "  `is_watching` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否正在追番'," +
                            "  `describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '动漫描述'," +
                            "  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "  PRIMARY KEY (`id`) USING BTREE" +
                            ") ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;";
                    stmt.executeUpdate(createmaintab);

                    //创建用户表
                    updateProgress(finalSession, 4, "创建用户表...");
                    String createusertab = "CREATE TABLE `users`  (" +
                            "  `uid` int(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
                            "  `username` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名(最大10字符)'," +
                            "  `password` char(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL," +
                            "  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "  PRIMARY KEY (`uid`) USING BTREE," +
                            "  UNIQUE INDEX `username`(`username`) USING BTREE" +
                            ") ENGINE = InnoDB AUTO_INCREMENT = 1001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = COMPACT;";
                    stmt.executeUpdate(createusertab);

                    //向用户表插入数据
                    updateProgress(finalSession, 5, "创建管理员账户: "+sysuser+"...");
                    String insertusersql = "INSERT INTO `users` (uid,username,password) VALUES(1000,?,?)";
                    PreparedStatement pst = conn.prepareStatement(insertusersql);
                    pst.setString(1, sysuser);
                    pst.setString(2, syspass);
                    pst.executeUpdate();
                    pst.close();

                    // 写入配置文件
                    String configContent = "db.url=jdbc:mysql://" + dbhost + ":" + dbport + "/limange\n" +
                            "db.user=" + dbuser + "\n" +
                            "db.password=" + dbpass + "\n";

                    String configPath = finalSession.getServletContext().getRealPath("/WEB-INF/db_config.properties");
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

                        // 设置文件权限（增强安全性）
                        //configFile.setReadable(true, true);
                        //configFile.setWritable(false, false); // 安装后设置为只读
                    } catch (IOException e) {
                        // 处理错误
                        Map<String, Object> progressData = (Map<String, Object>) finalSession.getAttribute("install_progress");
                        progressData.put("status", "error");
                        progressData.put("error", "安装失败: " + e.getMessage());
                    }

                    // 安装完成
                    updateProgress(finalSession, 6, "安装成功！");
                    Map<String, Object> finalProgress = (Map<String, Object>) finalSession.getAttribute("install_progress");
                    finalProgress.put("status", "completed");

                    // 写入安装完成状态
                    writeInstallStatus(finalSession.getServletContext(), 1);

                } catch (Exception e) {
                    // 处理错误
                    Map<String, Object> progressData = (Map<String, Object>) finalSession.getAttribute("install_progress");
                    progressData.put("status", "error");
                    progressData.put("error", "安装失败: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try {
                        if (stmt != null) stmt.close();
                    } catch (Exception e) {
                    }
                    try {
                        if (conn != null) conn.close();
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    // 进度查询API
    if ("progress".equals(request.getParameter("action"))) {
        Map<String, Object> progress = (Map<String, Object>) session.getAttribute("install_progress");
        response.setContentType("application/json");
        if (progress == null) {
            out.print("{\"status\":\"not_started\"}");
        } else {
            // 使用转义方法处理消息内容
            String status = (String) progress.get("status");
            String message = escapeJson((String) progress.get("message"));
            String error = escapeJson((String) progress.get("error"));

            out.print("{");
            out.print("\"status\":\"" + status + "\",");
            out.print("\"currentStep\":" + progress.get("currentStep") + ",");
            out.print("\"totalSteps\":" + progress.get("totalSteps") + ",");
            out.print("\"message\":\"" + message + "\"");

            if ("error".equals(status) && error != null) {
                out.print(",\"error\":\"" + error + "\"");
            }
            out.print("}");
        }
        return;
    }
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="css/install.css">
    <title>动漫记录Web应用程序安装向导</title>
    <script>
        // 全局上下文路径
        const contextPath = "<%= contextPath %>";
    </script>
</head>
<body>
<!--错误信息显示仅用于三四步数据为null时-->
<% if (session.getAttribute("install_error") != null) { %>
<div class="notification">
    <%= session.getAttribute("install_error") %>
</div>
<% session.removeAttribute("install_error"); %>
<% } %>
<!--主布局-->
<div class="install-container">
    <!--窗体上部分--->
    <div class="install-header">
        <div class="header-text">
            <h1>动漫记录Web应用程序安装向导</h1>
            <p>轻松完成应用程序安装与配置</p>
        </div>
    </div>

    <!--窗体中间部分--->
    <div class="install-body">
        <!--中间部分左侧边布局--->
        <div class="sidebar">
            <div class="step-item <%= "1".equals(step) ? "active" : "completed" %>">欢迎</div>
            <div class="step-item <%= "2".equals(step) ? "active" : (Integer.parseInt(step) > 2 ? "completed" : "") %>">许可协议</div>
            <div class="step-item <%= "3".equals(step) ? "active" : (Integer.parseInt(step) > 3 ? "completed" : "") %>">数据库配置</div>
            <div class="step-item <%= "4".equals(step) ? "active" : (Integer.parseInt(step) > 4 ? "completed" : "") %>">管理员账户</div>
            <div class="step-item <%= "5".equals(step) ? "active" : "" %>">安装系统</div>
        </div>

        <!--中间部分右侧边布局--->
        <div class="step-content">
            <%
                int installStatus = readInstallStatus(application);
                if (installStatus == 1) {
            %>
            <h2 class="step-title">您已完成安装</h2>
            <p class="step-description">您已完成安装请不要重复安装<br>请访问http://IP:端口号/limange/index.jsp以继续</p>
            <% return;}if ("1".equals(step))
            { %>
            <!-- 步骤1: 欢迎 -->
            <h2 class="step-title">欢迎使用动漫记录Web应用程序安装向导</h2>
            <p class="step-description">此向导将引导您完成动漫记录Web应用程序的安装过程。在继续之前，请确保您已准备好数据库连接信息和管理员账户信息。</p>

            <div style="margin-top: 220px; padding: 20px; background: #f8f9fa; border-radius: 8px;">
                <p><strong>注意：</strong> 安装过程中，向导将会在您的数据库中创建新表并插入初始数据。建议在继续之前备份您的数据库。</p>
            </div>

            <% }
            else if ("2".equals(step))
            { %>
            <!-- 步骤2: 许可协议 -->
            <h2 class="step-title">软件许可协议</h2>
            <div style="height: 300px; overflow-y: auto; border: 1px solid #e9ecef; padding: 20px; margin-bottom: 20px; border-radius: 8px;">
                <h3>动漫记录Web应用程序最终用户许可协议</h3>
                <p>感谢您选择动漫记录Web应用程序。请在使用本软件前仔细阅读以下协议内容：</p>

                <h4>1. 版权声明</h4>
                <p>本软件开源协议依于GPLv3</p>

                <h4>2. 使用许可</h4>
                <p>授权方授予您一项个人的、不可转让的、非排他性的许可，以使用本软件。您可以为非商业目的在一台计算机上安装、使用、访问、显示、运行本软件。</p>

                <h4>3. 限制条款</h4>
                <p>允许个人或组织在以下条件下使用:<br>1.非商业用途<br>2.不得修改版权声明<br>开源代码修改时必须标注原作者GitHub且必须开源</p>

                <h4>4. 免责声明</h4>
                <p>本软件按"原样"提供，不提供任何明示或暗示的担保，包括但不限于对适销性、特定用途适用性的担保。使用本软件的风险由您自行承担。</p>

                <h4>5. 终止条款</h4>
                <p>如您未遵守本协议条款，授权方有权终止本协议。协议终止后，您必须销毁本软件的所有副本。</p>

                <p style="margin-top: 20px;"><strong>版权所有 © 2020-2025 笙箫旧景。保留所有权利。</strong></p>
            </div>

            <div class="checkbox-container">
                <input type="checkbox" id="agree" name="agree" required>
                <label for="agree">我同意上述许可协议的所有条款</label>
            </div>

            <% }
            else if ("3".equals(step))
            { %>
            <!-- 步骤3: 数据库配置 -->
            <h2 class="step-title">数据库配置</h2>
            <p class="step-description">请提供您的数据库服务器信息。安装程序将使用这些信息创建数据库和表。</p>

            <form method="post" action="install.jsp?step=3" id="dbForm">
                <div class="form-group">
                    <label for="db_host">数据库服务器地址</label>
                    <input type="text" id="db_host" name="db_host" class="form-control" placeholder="例如: localhost" required>
                </div>

                <div class="form-group">
                    <label for="db_port">数据库端口</label>
                    <input type="number" id="db_port" name="db_port" class="form-control" value="3306" required>
                </div>

                <div class="form-group">
                    <label for="db_user">数据库用户名</label>
                    <input type="text" id="db_user" name="db_user" class="form-control" placeholder="具有创建数据库权限的用户" required>
                </div>

                <div class="form-group">
                    <label for="db_pass">数据库密码</label>
                    <input type="password" id="db_pass" name="db_pass" class="form-control" required>
                </div>

                <div style="margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px;">
                    <p><strong>注意：</strong> 安装程序将创建名为 <code>limange</code> 的数据库。如果该数据库已存在，其内容将被覆盖。</p>
                </div>
                    <button type="submit" form="dbForm" class="btn btn-primary">下一步</button>
            </form>

            <% }
            else if ("4".equals(step))
            { %>
            <!-- 步骤4: 管理员账户 -->
            <h2 class="step-title">创建管理员账户</h2>
            <p class="step-description">请设置系统管理员账户，此账户将拥有系统最高权限。</p>

            <form method="post" action="install.jsp?step=4" id="adminForm">
                <div class="form-group">
                    <label for="admin_user">管理员用户名</label>
                    <input type="text" id="admin_user" name="admin_user" class="form-control" placeholder="输入用户名" required>
                </div>

                <div class="form-group">
                    <label for="admin_pass">管理员密码</label>
                    <input type="password" id="admin_pass" name="admin_pass" class="form-control" placeholder="输入密码" required>
                </div>

                <div class="form-group">
                    <label for="admin_pass_confirm">确认密码</label>
                    <input type="password" id="admin_pass_confirm" name="admin_pass_confirm" class="form-control" placeholder="再次输入密码" required>
                </div>

                <div style="margin-top: 30px; padding: 20px; background: #f8f9fa; border-radius: 8px;">
                    <p><strong>安全提示：</strong> 请使用强密码，包含大小写字母、数字和特殊字符，长度至少为8位。</p>
                </div>
                <button type="submit" form="adminForm" class="btn btn-primary">开始安装</button>
            </form>

            <% }
            else if ("5".equals(step))
            { %>
            <!-- 步骤5: 安装 -->
            <h2 class="step-title">正在安装动漫记录Web应用程序</h2>
            <p class="step-description">请稍候，安装中。此过程可能需要几分钟时间。</p>

            <!-- 错误信息容器 -->
            <div id="errorContainer" style="display:none; color:red; padding:10px; background:#ffecec; border:1px solid red; margin-bottom:15px;"></div>

            <div class="progress-container">
                <div class="progress-bar">
                    <div class="progress-fill" id="progressFill"></div>
                </div>
                <div class="progress-text" id="progressText">正在开始安装...</div>
            </div>

            <div class="install-log" id="installLog">
                <div class="log-entry">=== 开始安装  ===</div>
            </div>
            <% } %>

        </div>
    </div>

    <!--窗体下部分--->
    <div class="install-footer">
        <div>
            <% if ("2".equals(step)||"3".equals(step)||"4".equals(step)) { %>
            <a href="install.jsp?step=<%= Integer.parseInt(step)-1 %>" class="btn btn-secondary">上一步</a>
            <% } %>
        </div>
        <div>
            <% if ("1".equals(step)) { %>
            <a href="install.jsp?step=2" class="btn btn-primary">开始安装</a>
            <% }
            else if ("2".equals(step)) { %>
            <a href="install.jsp?step=3" class="btn btn-primary" id="agreeBtn">下一步</a>
            <% } else if ("5".equals(step)) { %>
            <a href="<%=contextPath%>/login.jsp" class="btn btn-primary" id="finishBtn" style="display: none;">完成</a>
            <% } %>
        </div>
    </div>

</div>

<% if ("2".equals(step)) { %>
<script>
    document.getElementById('agreeBtn').addEventListener('click', function(e) {
        if (!document.getElementById('agree').checked) {
            alert('请先同意许可协议！');
            e.preventDefault();
        }
    });
</script>
<% } %>

<% if ("4".equals(step)) { %>
<script>
    document.querySelector('form').addEventListener('submit', function(e) {
        const pass1 = document.getElementById('admin_pass').value;
        const pass2 = document.getElementById('admin_pass_confirm').value;

        if (pass1 !== pass2) {
            alert('两次输入的密码不一致！');
            e.preventDefault();
        } else if (pass1.length < 8) {
            alert('密码长度至少需要8个字符！');
            e.preventDefault();
        }
    });
</script>
<% } %>

<% if ("5".equals(step)) { %>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const progressFill = document.getElementById('progressFill');
        const progressText = document.getElementById('progressText');
        const installLog = document.getElementById('installLog');
        const finishBtn = document.getElementById('finishBtn');
        const errorContainer = document.getElementById('errorContainer');

        // 安装进度监控
        function monitorInstallation() {
            fetch('install.jsp?action=progress')
                .then(response => response.json())
                .then(data => {
                    console.log('进度数据:', data);

                    // 始终更新进度条
                    const percent = (data.currentStep / data.totalSteps) * 100;
                    progressFill.style.width = percent + '%';
                    progressText.textContent = data.message || "";

                    // 添加新日志（避免重复）
                    const lastLog = installLog.lastChild?.textContent;
                    if (data.message && lastLog !== data.message) {
                        const logEntry = document.createElement('div');
                        logEntry.className = 'log-entry';
                        logEntry.textContent = data.message;
                        installLog.appendChild(logEntry);
                        installLog.scrollTop = installLog.scrollHeight;
                    }

                    // 处理状态
                    if (data.status === 'running') {
                        setTimeout(monitorInstallation, 500);
                    } else if (data.status === 'completed') {
                        const successEntry = document.createElement('div');
                        successEntry.className = 'log-entry success';
                        successEntry.textContent = '安装成功！已准备就绪。';
                        installLog.appendChild(successEntry);
                        finishBtn.style.display = 'inline-block';
                    } else if (data.status === 'error') {
                        errorContainer.textContent = data.error || "未知错误";
                        errorContainer.style.display = 'block';
                        const errorEntry = document.createElement('div');
                        errorEntry.className = 'log-entry error';
                        errorEntry.textContent = '安装失败: ' + (data.error || "未知错误");
                        installLog.appendChild(errorEntry);
                    } else if (data.status === 'not_started') {
                        setTimeout(monitorInstallation, 1000);
                    }
                })
                .catch(error => {
                    console.error('安装监控错误:', error);
                    const errorEntry = document.createElement('div');
                    errorEntry.className = 'log-entry error';
                    errorEntry.textContent = '监控请求失败: ' + error.message;
                    installLog.appendChild(errorEntry);
                });
        }
        // 开始监控安装进度
        monitorInstallation();
    });
</script>
<% } %>
<div class="version-info">
    <!--获取版权信息-->
    <span>软件版本<%=systemInfo.getSystemVersion()%></span><br>
    <span>Anime Records <%=systemInfo.getCopyrightInfo()%></span><br>
    <span><%=systemInfo.getAuthorGithub()%></span>
</div>
</body>
</html>