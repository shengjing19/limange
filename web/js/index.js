/*
主页面 js文件
by shengjing19
create 2025-07-01
last modify 2025-08-07
v1.2
* */
// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 滚动时隐藏顶部栏
    let lastScrollTop = 0;
    const header = document.querySelector('header');

    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

        if (scrollTop > lastScrollTop && scrollTop > 100) {
            // 向下滚动超过100px
            header.classList.add('hidden');
        } else {
            // 向上滚动
            header.classList.remove('hidden');
        }
        lastScrollTop = scrollTop;
    });

    // 添加下拉菜单项的点击事件
    document.querySelectorAll('.menu-items li').forEach(item => {
        item.addEventListener('click', function() {
            const action = this.querySelector('span').textContent;
            //alert(`您点击了: ${action}`);

            // 如果是退出登录，则重定向到登录页
            if (action === '退出登录')
            {
                fetch(contextPath +'/logout', {
                    method: 'POST',
                    headers: {
                        //'${_csrf.headerName}': '${_csrf.token}' // CSRF令牌
                    }
                })
                    .then(() => window.location.href = 'login.jsp') // 退出后跳转登录页
                    .catch(e => console.error('退出失败:', e));
            }
            else if(action==='数据管理')
            {
                window.location.href = 'admin.jsp'
            }
        });
    });

    // 绑定侧边栏菜单点击事件
    document.querySelectorAll('.sidebar-menu li').forEach(menuItem => {
        menuItem.addEventListener('click', function() {
            // 设置当前菜单项为active
            document.querySelectorAll('.sidebar-menu li').forEach(item => {
                item.classList.remove('active');
            });
            this.classList.add('active');

            // 获取标签类型
            const tabName = this.getAttribute('data-tab');

            // 显示加载动画
            const contentSection = document.querySelector('.content-section');
            contentSection.innerHTML = `
                <div class="loader">
                    <div class="loader-circle"></div>
                </div>
            `;

            initHomeStats();

            // 根据不同标签显示内容
            if (tabName === 'stats') {
                renderStatsScreen();
            }
            else {
                // 发送AJAX请求获取数据
                fetch(contextPath + '/anime-main?type=' + tabName)
                    .then(response => response.json())
                    .then(data => {
                        if (tabName === 'finished') {
                            renderFinishedAnimes(data);
                        } else if (tabName === 'favorite') {
                            renderFavoriteAnimes(data);
                        } else if (tabName === 'watching') {
                            renderWatchingAnimes(data);
                        }
                        bindAnimeCardEvents();
                    })
                    .catch(error => {
                        console.error('加载数据失败:', error);
                        contentSection.innerHTML = '<p class="error">加载数据失败，请稍后再试</p>';
                    });
            }
        });
    });

    // 默认加载"主页"内容
    document.querySelector('.sidebar-menu li[data-tab="finished"]').click();

});

// 渲染已看完动漫
function renderFinishedAnimes(data) {
    const contentSection = document.querySelector('.content-section');
    let html = `
        <div class="section-header">
            <h2 class="section-title"><i class="fas fa-check-circle"></i> 已看完的动漫</h2>
            <div>
                <span>共${data.total}部</span>
            </div>
        </div>
    `;

    // 分组显示
    data.groupedAnimes.forEach(yearGroup => {
        html += `
            <div class="time-group">
                <div class="group-header">
                    <h3 class="group-title">${yearGroup.year}</h3>
                    <span class="group-date">${yearGroup.month}</span>
                    <span style="margin-left: 10px;color: rgba(147, 112, 219, 0.58)">本月已看${yearGroup.count}部</span>
                </div>
                <div class="anime-grid">
        `;

        yearGroup.animes.forEach(anime => {
            html += `
                <div class="anime-card" data-id="${anime.id}">
                    <img src="${contextPath}/${anime.coverImage}" alt="${anime.title}" class="anime-cover">
                    <div class="anime-info">
                        <div class="anime-title">${anime.title}</div>
                        <div class="anime-meta">
                            <span>${anime.totalEpisodes}集</span>
                            <span>${anime.finishDate}</span>
                        </div>
                    </div>
                </div>
            `;
        });

        html += `
                </div>
            </div>
        `;
    });

    contentSection.innerHTML = html;
}


// 渲染最喜欢的动漫
function renderFavoriteAnimes(data) {
    const contentSection = document.querySelector('.content-section');
    let html = `
        <div class="section-header">
            <h2 class="section-title"><i class="fas fa-heart"></i> 最喜欢的动漫</h2>
            <div>
                <span>共${data.total}部</span>
            </div>
        </div>
        <div class="anime-grid">
    `;

    data.animes.forEach(anime => {
        html += `
            <div class="anime-card" data-id="${anime.id}">
                <img src="${contextPath}/${anime.coverImage}" alt="${anime.title}" class="anime-cover">
                <div class="anime-info">
                    <div class="anime-title">${anime.title}</div>
                    <div class="anime-meta">
                        <span>${anime.totalEpisodes}集</span>
                        <span>${anime.finishDate}</span>
                    </div>
                </div>
            </div>
        `;
    });

    html += `</div>`;
    contentSection.innerHTML = html;
}

// 渲染正在追的动漫
function renderWatchingAnimes(data) {
    const contentSection = document.querySelector('.content-section');
    let html = `
        <div class="section-header">
            <h2 class="section-title"><i class="fas fa-running"></i> 正在追的动漫</h2>
            <div>
                <span>共${data.total}部</span>
            </div>
        </div>
        <div class="anime-grid">
    `;

    data.animes.forEach(anime => {
        html += `
            <div class="anime-card" data-id="${anime.id}">
                <img src="${contextPath}/${anime.coverImage}" alt="${anime.title}" class="anime-cover">
                <div class="anime-info">
                    <div class="anime-title">${anime.title}</div>
                    <div class="anime-meta">
                        <span>${anime.totalEpisodes}集</span>
                        <span>更新中</span>
                    </div>
                </div>
            </div>
        `;
    });

    html += `</div>`;
    contentSection.innerHTML = html;
}

//从服务端获取统计数据
function initHomeStats() {
    fetch(contextPath + '/anime-main?type=stats')
        .then(response => response.json())
        .then(data => {
            statsData.totalFinished = data.totalFinished;
            statsData.totalFavorite = data.totalFavorite;
            statsData.totalWatching = data.totalWatching;
        });
}

// 渲染统计大屏
function renderStatsScreen() {
    const contentSection = document.querySelector('.content-section');
    const html = `
        <div class="stats-header">
            <h2><i class="fas fa-chart-bar"></i> 数据统计大屏</h2>
            <div>
                <span>数据实时更新</span>
            </div>
        </div>
        
        <!-- 统计卡片 -->
        <div class="stats-grid">
            <div class="stat-card">
                <i class="fas fa-eye"></i>
                <h3>${statsData.totalFinished}</h3>
                <p>已看完动漫</p>
            </div>
            <div class="stat-card">
                <i class="fas fa-heart"></i>
                <h3>${statsData.totalFavorite}</h3>
                <p>最喜欢动漫</p>
            </div>
            <div class="stat-card">
                <i class="fas fa-running"></i>
                <h3>${statsData.totalWatching}</h3>
                <p>正在追番</p>
            </div>
            <div class="stat-card">
                <i class="fas fa-calendar-alt"></i>
                <h3>建设中..</h3>
                <p>总观看天数</p>
            </div>
        </div>
        
        <!-- 图表区域 -->
        <div class="stats-chart-container">
            <div class="stats-chart">
                <h3><i class="fas fa-calendar-week"></i> 本周观看统计</h3>
                <div class="chart-container">
                    <p>图表功能建设中...</p>
                </div>
            </div>
            
            <div class="stats-chart">
                <h3><i class="fas fa-chart-line"></i> 年度观看趋势</h3>
                <div class="chart-container">
                    <p>图表功能建设中...</p>
                </div>
            </div>
            
        </div>
        
       
    `;

    contentSection.innerHTML = html;
}

// 绑定动漫卡片点击事件
function bindAnimeCardEvents() {
    document.querySelectorAll('.anime-card').forEach(card => {
        card.addEventListener('click', function() {
            const animeId = this.getAttribute('data-id');

            // 发送AJAX请求获取动漫详情
            fetch(contextPath + '/anime-detail?id=' + animeId)
                .then(response => response.json())
                .then(anime => {
                    document.getElementById('modalTitle').textContent = anime.title;
                    document.getElementById('modalCover').src = contextPath + '/' + anime.coverImage;
                    document.getElementById('modalCover').alt = anime.title;
                    document.getElementById('modalEpisodes').textContent = anime.totalEpisodes + '集';
                    document.getElementById('modalDate').textContent = anime.finishDate;
                    document.getElementById('modalFavorite').textContent = anime.isFavorite ? '是' : '否';
                    document.getElementById('modalDescription').textContent = anime.description;

                    // 显示弹窗
                    document.getElementById('animeModal').classList.add('active');
                })
                .catch(error => {
                    console.error('Error fetching anime details:', error);
                });
        });
    });
}

// 关闭弹窗
document.getElementById('closeModal').addEventListener('click', function() {
    document.getElementById('animeModal').classList.remove('active');
});

// 点击模态框外部关闭
window.addEventListener('click', function(event) {
    const animeModal = document.getElementById('animeModal');

    if (event.target === animeModal) {
        animeModal.classList.remove('active');
        document.body.style.overflow = '';
    }

});

// 初始化绑定事件
document.addEventListener('DOMContentLoaded', function() {
    // 绑定初始动漫卡片事件
    bindAnimeCardEvents();

    // 绑定标签切换事件
    document.querySelectorAll('.tab').forEach(tab => {
        if (tab.classList.contains('active')) {
            tab.click();
        }
    });
});
