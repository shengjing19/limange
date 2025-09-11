/*
数据管理页面 js文件
by shengjing19
create 2025-07-10
last modify 2025-08-27
v1.7
* */
// 显示通知
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

//=============================================================主接口服务(添加、修改、获得、删除)=====================================================

// 添加动漫处理
function initAnimeForm() {
    const animeForm = document.getElementById('animeForm');
    if (!animeForm) return;

    animeForm.addEventListener('submit', async function(event) {
        event.preventDefault(); // 阻止默认表单提交

        // 显示加载状态
        const submitBtn = animeForm.querySelector('button[type="submit"]');
        const originalBtnText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 添加中...';
        submitBtn.disabled = true;

        //AJAX
        try {
            const formData = new FormData(this);
            const response = await fetch('add-anime', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                showNotification('动漫添加成功', 'success');

                // 清空表单
                this.reset();
                const previewImage = document.getElementById('previewImage');
                previewImage.src = '';
                previewImage.style.display = 'none';

            }
            else {
                const error = await response.text();
                if (response.status === 403)
                {
                    showNotification('文件安全性检测不通过', 'error');
                }
                else if(response.status===400)
                {
                    showNotification('日期格式不正确', 'error');
                }
                else
                {
                    showNotification(`添加失败`, 'error');
                }
            }
        } catch (error) {
            showNotification(`网络错误: ${error.message}`, 'error');
        } finally {
            // 恢复按钮状态
            submitBtn.innerHTML = originalBtnText;
            submitBtn.disabled = false;
        }
    });
}

// 修改动漫处理
function initEditForm() {
    const editForm = document.getElementById('editForm');
    if (!editForm) return;

    editForm.addEventListener('submit', async function(event) {
        event.preventDefault();

        // 显示加载状态
        const submitBtn = editForm.querySelector('button[type="submit"]');
        const originalBtnText = submitBtn.innerHTML;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 保存中...';
        submitBtn.disabled = true;

        //AJAX
        try {
            const formData = new FormData(this);
            const response = await fetch('update-anime', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                showNotification('动漫更新成功', 'success');
                document.getElementById('editModal').classList.remove('active');
                refreshCurrentList();
            }
            else {
                const error = await response.text();
                if (response.status === 403)
                {
                    showNotification('文件安全性检测不通过', 'error');
                }
                else if(response.status===400)
                {
                    //400在这里面也有ID的错误，但是一般正常使用不会触发，这里就不做描述
                    showNotification('日期格式不正确', 'error');
                }
                else
                {
                    showNotification(`添加失败`, 'error');
                }
            }
        } catch (error) {
            showNotification(`网络错误: ${error.message}`, 'error');
        } finally {
            // 恢复按钮状态
            submitBtn.innerHTML = originalBtnText;
            submitBtn.disabled = false;
        }
    });
}

// 打开修改动漫窗口并获取当前ID的初始动漫信息
function openEditModal(id) {
    console.log('打开编辑窗口，ID:', id);
    //AJAX
    fetch('get-anime?id=' + id)
        .then(response => {
            if (!response.ok) {
                //404与400都是非正常访问引起，这里统一归为网络响应异常去触发加载动漫信息失败
                throw new Error('网络响应异常');
            }
            return response.json();
        })
        .then(anime => {
            console.log('获取到的动漫数据:', anime);

            // 填充表单
            document.getElementById('editId').value = anime.id;
            document.getElementById('editTitle').value = anime.title;
            document.getElementById('editEpisodes').value = anime.totalEpisodes;
            document.getElementById('editFinishDate').value = anime.finishDate;
            document.getElementById('editIsFavorite').checked = anime.isFavorite;
            document.getElementById('editIsWatching').checked = anime.isWatching;
            document.getElementById('editDescription').value = anime.description || '';

            // 显示当前封面
            if (anime.coverImage) {
                document.getElementById('editPreviewImage').src = anime.coverImage;
                document.getElementById('editPreviewImage').style.display = 'block';
            }

            // 显示编辑窗口 - 使用 active 类
            document.getElementById('editModal').classList.add('active');
            console.log('编辑窗口已显示');
        })
        .catch(error => {
            console.error('加载动漫信息失败:', error);
            showNotification('加载动漫信息失败: ' + error.message, 'error');
        });
}

// 删除动漫处理
function deleteAnime(id) {
    console.log('删除动漫，ID:', id);
    if (!confirm('确定要删除这条动漫记录吗？确定后与之对应的封面文件也会被删除，此操作不可撤销。'))
    {
        return;
    }

    //AJAX
    fetch('delete-anime', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: 'id=' + id
    })
        .then(response =>
        {
            if (response.ok)
            {
                showNotification('删除成功', 'success');
                // 刷新当前显示的列表
                refreshCurrentList();
            }
            else
            {
                //400在这里面也有ID的错误，但是一般正常使用不会触发，这里就不做描述
                showNotification('删除失败', 'error');
            }
        })
        .catch(error => {
            console.error('删除失败:', error);
            showNotification('删除失败: ' + error.message, 'error');
        });
}

// 从服务器获取动漫列表
async function fetchAnimeList(type = 'all', page = 1) {
    //AJAX
    try {
        const response = await fetch(`admin-anime-list?type=${type}&page=${page}`);
        if (response.ok) {
            return await response.json();
        }
        else {
            throw new Error('获取动漫列表失败');
        }
    } catch (error) {
        console.error('Error fetching anime list:', error);
        showNotification('获取动漫列表失败', 'error');
        return { animes: [], totalPages: 0, currentPage: 1, totalCount: 0 };
    }
}

// 渲染动漫列表
function renderAnimeList(animes, tableBodyId) {
    const tableBody = document.getElementById(tableBodyId);
    if (!tableBody) return;

    tableBody.innerHTML = '';

    if (animes.length === 0) {
        // 根据表格ID决定列数
        const colSpan = tableBodyId === 'animeTableBody' ? 7 : 5;
        tableBody.innerHTML = '<tr><td colspan="${colSpan}" style="text-align: center;">暂无动漫记录</td></tr>';
        return;
    }

    // 直接遍历数组，不进行排序
    animes.forEach(anime => {
        const row = document.createElement('tr');
        row.setAttribute('data-id', anime.id);

        // 基础HTML内容
        let rowHtml = `
            <td><img src="${anime.coverImage}" alt="封面" class="admin-cover"></td>
            <td>${anime.title}</td>
            <td>${anime.totalEpisodes}</td>
            <td>${anime.finishDate}</td>
        `;

        // 根据表格ID决定是否显示"最喜欢"和"正在追"列
        if (tableBodyId === 'animeTableBody') {
            rowHtml += `
                <td>${anime.isFavorite ? "<i class='fas fa-heart' style='color: red;'></i>" : ""}</td>
                <td>${anime.isWatching ? "<i class='fas fa-eye' style='color: green;'></i>" : ""}</td>
            `;
        }

        // 操作按钮
        rowHtml += `
            <td>
                <button class="btn-action edit" data-id="${anime.id}"><i class="fas fa-edit"></i></button>
                <button class="btn-action delete" data-id="${anime.id}"><i class="fas fa-trash"></i></button>
            </td>
        `;

        row.innerHTML = rowHtml;
        tableBody.appendChild(row);
    });
}

// 初始化分页功能
function initPagination(tableBodyId, paginationId, totalPages, currentPage, type) {
    const pagination = document.getElementById(paginationId);
    if (!pagination) return;

    // 清空分页控件
    pagination.innerHTML = '';

    if (totalPages <= 1) return;

    // 添加上一页按钮
    const prevButton = document.createElement('button');
    prevButton.innerHTML = '<i class="fas fa-chevron-left"></i>';
    prevButton.disabled = currentPage === 1;
    prevButton.addEventListener('click', () => {
        changePage(tableBodyId, paginationId, currentPage - 1, type);
    });
    pagination.appendChild(prevButton);

    // 智能分页逻辑
    if (totalPages <= 7) {
        // 如果总页数小于等于7，显示所有页码
        for (let i = 1; i <= totalPages; i++) {
            createPageButton(i, currentPage, tableBodyId, paginationId, type, pagination);
        }
    } else {
        // 总页数大于7，实现智能省略
        createPageButton(1, currentPage, tableBodyId, paginationId, type, pagination);

        if (currentPage > 4) {
            const ellipsis1 = document.createElement('span');
            ellipsis1.textContent = '...';
            ellipsis1.className = 'pagination-ellipsis';
            pagination.appendChild(ellipsis1);
        }

        // 计算要显示的页码范围
        let startPage = Math.max(2, currentPage - 2);
        let endPage = Math.min(totalPages - 1, currentPage + 2);

        // 调整范围以确保显示足够的页码
        if (currentPage <= 4) {
            endPage = 5;
        } else if (currentPage >= totalPages - 3) {
            startPage = totalPages - 4;
        }

        // 添加中间页码
        for (let i = startPage; i <= endPage; i++) {
            createPageButton(i, currentPage, tableBodyId, paginationId, type, pagination);
        }

        if (currentPage < totalPages - 3) {
            const ellipsis2 = document.createElement('span');
            ellipsis2.textContent = '...';
            ellipsis2.className = 'pagination-ellipsis';
            pagination.appendChild(ellipsis2);
        }

        createPageButton(totalPages, currentPage, tableBodyId, paginationId, type, pagination);
    }

    // 添加下一页按钮
    const nextButton = document.createElement('button');
    nextButton.innerHTML = '<i class="fas fa-chevron-right"></i>';
    nextButton.disabled = currentPage === totalPages;
    nextButton.addEventListener('click', () => {
        changePage(tableBodyId, paginationId, currentPage + 1, type);
    });
    pagination.appendChild(nextButton);

    // 设置当前页码
    pagination.dataset.currentPage = currentPage;
    pagination.dataset.totalPages = totalPages;
    pagination.dataset.type = type;
}

// 创建页码按钮的辅助函数
function createPageButton(pageNum, currentPage, tableBodyId, paginationId, type, paginationContainer) {
    const pageButton = document.createElement('button');
    pageButton.textContent = pageNum;
    pageButton.classList.toggle('active', pageNum === currentPage);
    pageButton.addEventListener('click', () => {
        changePage(tableBodyId, paginationId, pageNum, type);
    });
    paginationContainer.appendChild(pageButton);
}

// 更改页面
async function changePage(tableBodyId, paginationId, page, type) {
    const data = await fetchAnimeList(type, page);
    renderAnimeList(data.animes, tableBodyId);

    const pagination = document.getElementById(paginationId);
    if (pagination) {
        initPagination(tableBodyId, paginationId, data.totalPages, page, type);
    }
}

// 加载动漫数据
async function loadAnimeData(section) {
    let type = 'all';
    let tableBodyId = 'animeTableBody';
    let paginationId = 'pagination';

    if (section === 'anime-list-love-section') {
        type = 'favorite';
        tableBodyId = 'animeTableBody-love';
        paginationId = 'pagination-love';
    } else if (section === 'anime-list-iswatching-section') {
        type = 'watching';
        tableBodyId = 'animeTableBody-iswatching';
        paginationId = 'pagination-iswatching';
    }

    const data = await fetchAnimeList(type, 1);
    renderAnimeList(data.animes, tableBodyId);
    initPagination(tableBodyId, paginationId, data.totalPages, 1, type);
}

// 刷新当前显示的列表
function refreshCurrentList() {
    const activeSection = document.querySelector('.content-section.active');
    if (!activeSection) return;

    const sectionId = activeSection.id;

    if (sectionId !== 'add-anime-section') {
        loadAnimeData(sectionId);
    }
}

//===========================================前端验证(可保留可不保留，后端已有，这里保留可以增加二次验证)==========================================
// 初始化图片上传功能(前端)
function initImageUpload(uploadAreaId, fileInputId, previewImageId) {
    // 获取DOM元素
    const uploadArea = document.getElementById(uploadAreaId);
    const fileInput = document.getElementById(fileInputId);
    const previewImage = document.getElementById(previewImageId);

    // 确保所有必需元素都存在
    if (uploadArea && fileInput && previewImage)
    {
        // 点击上传区域触发文件选择
        uploadArea.addEventListener('click', () =>
        {
            fileInput.click();
        });

        // 监听文件选择变化
        fileInput.addEventListener('change', function()
        {
            // 获取选择的第一个文件
            const file = this.files[0];
            if (file)
            {
                // 检查文件类型
                if (!file.type.match('image.*'))
                {
                    showNotification('请选择图片文件', 'error');
                    return;
                }

                // 检查文件大小 (限制为2MB)
                if (file.size > 2 * 1024 * 1024)
                {
                    showNotification('图片大小不能超过2MB', 'error');
                    return;
                }

                // 使用FileReader读取文件
                const reader = new FileReader();
                reader.onload = function(e)
                {
                    // 显示预览图
                    previewImage.src = e.target.result;
                    previewImage.style.display = 'block';
                }
                reader.readAsDataURL(file);// 以DataURL格式读取
            }
        });
    }
}

//================================================页面初始化==================================================================
// 初始化管理页面
async function initAdminPage() {

    // 初始化表单提交处理
    initAnimeForm();
    initEditForm();

    // 初始化图片上传功能
    initImageUpload('uploadArea', 'coverUpload', 'previewImage');
    initImageUpload('editUploadArea', 'editCoverUpload', 'editPreviewImage');

    // 加载初始数据
    await loadAnimeData('add-anime-section');

    // 初始化菜单切换
    document.querySelectorAll('.sidebar-menu li').forEach(menuItem => {
        menuItem.addEventListener('click', function() {
            // 设置当前菜单项为active
            document.querySelectorAll('.sidebar-menu li').forEach(item => {
                item.classList.remove('active');
            });
            this.classList.add('active');

            // 显示对应的内容区域
            const sectionId = this.getAttribute('data-section');
            document.querySelectorAll('.content-section').forEach(section => {
                section.classList.remove('active');
            });

            // 处理不同的区域ID
            let targetSectionId = sectionId;
            if (sectionId === 'anime-list-love' || sectionId === 'anime-list-iswatching') {
                targetSectionId += '-section';
            } else {
                targetSectionId += '-section';
            }

            const targetSection = document.getElementById(targetSectionId);
            targetSection.classList.add('active');

            // 加载对应类型的数据
            loadAnimeData(targetSectionId);

        });
    });

    // 全局事件委托处理操作按钮
    document.addEventListener('click', function(event) {
        // 处理编辑按钮
        if (event.target.classList.contains('edit') || event.target.closest('.btn-action.edit'))
        {
            const btn = event.target.closest('.btn-action.edit');
            if (btn) {
                const id = btn.getAttribute('data-id');
                openEditModal(id);
                event.preventDefault();
            }
        }

        // 处理删除按钮
        if (event.target.classList.contains('delete') || event.target.closest('.btn-action.delete'))
        {
            const btn = event.target.closest('.btn-action.delete');
            if (btn) {
                const id = btn.getAttribute('data-id'); //获取要删除动漫的ID
                deleteAnime(id);//调用删除动漫函数
                event.preventDefault();
            }
        }

        // 关闭模态框
        if (event.target.classList.contains('close') || event.target.closest('.modal-close'))
        {
            document.getElementById('editModal').classList.remove('active');
        }
    });

    // 点击模态框外部关闭
    window.addEventListener('click', function(event) {
        const modal = document.getElementById('editModal');
        if (event.target === modal) {
            modal.classList.remove('active');
        }
    });

}

// 当DOM加载完成后初始化页面
document.addEventListener('DOMContentLoaded', initAdminPage);