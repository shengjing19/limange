/*
* 恢复页面js
* by shengjing19
* create 2025-9-4
* last modify
* v1.0*/

function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);

    // 添加淡入效果
    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'translateY(0)';
    }, 10);

    // 3秒后移除通知
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateY(-20px)';
        setTimeout(() => notification.remove(), 500);
    }, 3000);
}

document.addEventListener('DOMContentLoaded', function() {
    // 获取元素
    const databaseCard = document.getElementById('database-card');
    const passwordCard = document.getElementById('password-card');
    const databaseModal = document.getElementById('database-modal');
    const passwordModal = document.getElementById('password-modal');
    const closeButtons = document.querySelectorAll('.close-button');

    // 点击数据库卡片显示模态窗口
    databaseCard.addEventListener('click', function(e) {
        e.preventDefault();
        databaseModal.style.display = 'flex';
    });

    // 点击密码卡片显示模态窗口
    passwordCard.addEventListener('click', function(e) {
        e.preventDefault();
        passwordModal.style.display = 'flex';
    });

    // 点击关闭按钮隐藏模态窗口
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            databaseModal.style.display = 'none';
            passwordModal.style.display = 'none';
        });
    });

    // 点击模态窗口外部关闭窗口
    window.addEventListener('click', function(e) {
        if (e.target === databaseModal) {
            databaseModal.style.display = 'none';
        }
        if (e.target === passwordModal) {
            passwordModal.style.display = 'none';
        }
    });

    // 数据库表单提交 - 使用AJAX实现局部刷新
    document.getElementById('database-form').addEventListener('submit', function(e) {
        e.preventDefault();

        // 收集表单数据
        const formData = new URLSearchParams();
        formData.append('db-address', document.getElementById('db-address').value);
        formData.append('db-port', document.getElementById('db-port').value);
        formData.append('db-username', document.getElementById('db-username').value);
        formData.append('db-password', document.getElementById('db-password').value);

        // 使用AJAX提交表单

        fetch('', { // 空字符串表示提交到当前URL
            method: 'POST',
            body: formData,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest' // 添加请求头标识为AJAX请求
            }
        })
            .then(response => {
                if(response.ok) {
                    // 显示成功消息
                    showNotification('数据库配置已成功更新！', 'success');
                    // 关闭窗口
                    databaseModal.style.display = 'none';
                    // 重置表单
                    this.reset();
                }
                else {
                    response.text().then(errorText =>{
                        if(response.status===400) {
                            showNotification('数据库配置出错:输入的值不能为null！', 'error');
                        }
                        else if(response.status===403){
                            showNotification('数据库配置出错:服务器已关闭恢复模式,状态值已置为1!', 'error');
                        }
                        else{
                            showNotification('数据库配置出错:'+errorText, 'error');
                        }
                    });
                }
            })
            .catch(error => {
                // 显示错误消息
                showNotification('保存失败: ' + error, 'error');
            });
    });

    //密码表单提交
    document.getElementById('password-form').addEventListener('submit', function(e) {
        e.preventDefault();

        const newPassword = document.getElementById('new-password').value;
        const confirmPassword = document.getElementById('confirm-password').value;

        if (newPassword !== confirmPassword) {
            alert('两次输入的密码不一致，且该功能暂未开发！');
            return;
        }

        alert('该功能暂未开发！');
        passwordModal.style.display = 'none';
    });
});