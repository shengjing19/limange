/*
登录 js文件
by shengjing19
create 2025-07-15
last modify 2025-08-07
v1.1
* */
document.addEventListener('DOMContentLoaded', function() {
    const popupOverlay = document.getElementById('welcome-page');
    // 页面加载后自动打开弹窗(可用时) 欢迎页
    popupOverlay.classList.add('active');
    document.body.style.overflow = 'hidden';
});

// 登录表单提交
document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault(); // 阻止表单默认提交

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // 清除之前的错误信息
    document.getElementById('usernameError').textContent = '';
    document.getElementById('passwordError').textContent = '';

    // 发送AJAX请求
    fetch(contextPath + '/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
    })
        .then(response => {
            if (response.ok)
            {
                // 登录成功，重定向到管理页面
                window.location.href = contextPath + '/index.jsp';
            }
            else
            {
                // 登录失败，显示错误信息
                return response.text().then(text => {
                    throw new Error(text || '用户名或密码错误');
                });
            }
        })
        .catch(error => {
            // 显示错误提示
            const errorMessage = error.message || '用户名或密码错误';
            // 创建错误提示元素
            const errorElement = document.createElement('div');
            errorElement.className = 'notification error';
            errorElement.innerHTML = `
            <i class="fas fa-exclamation-circle"></i>
            <span>${errorMessage}</span>
        `;

            // 错误提示元素添加到body
            document.body.appendChild(errorElement);

            // 添加淡入效果
            setTimeout(() => {
                errorElement.style.opacity = '1';
                errorElement.style.transform = 'translateY(0)';
            }, 10);

            // 3秒后移除通知
            setTimeout(() => {
                errorElement.style.opacity = '0';
                errorElement.style.transform = 'translateY(-20px)';
                setTimeout(() => errorElement.remove(), 500);
            }, 3000);

        });
});




