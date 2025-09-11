/*
* 流动背景
* by shengjing19
* create 2025-8-9
* last modify
* v1.0*/
document.addEventListener('DOMContentLoaded', () => {
    // 图片URL数组 
    const imageUrls = [
        contextPath + '/img/anime/1.jpg',
        contextPath + '/img/anime/2.jpg',
        contextPath + '/img/anime/3.jpg',
        contextPath + '/img/anime/4.jpg',
        contextPath + '/img/anime/5.jpg',
        contextPath + '/img/anime/6.jpg',
        contextPath + '/img/anime/7.jpg',
        contextPath + '/img/anime/8.jpg',
        contextPath + '/img/anime/9.jpg',
        contextPath + '/img/anime/10.jpg',
        contextPath + '/img/anime/11.jpg',
        contextPath + '/img/anime/12.jpg',
        contextPath + '/img/anime/13.jpg',
        contextPath + '/img/anime/14.jpg',
        contextPath + '/img/anime/15.jpg',
        contextPath + '/img/anime/16.jpg',
        contextPath + '/img/anime/17.jpg',
        contextPath + '/img/anime/18.jpg',
        contextPath + '/img/anime/19.jpg',
        contextPath + '/img/anime/20.jpg',
        contextPath + '/img/anime/21.jpg'
    ];

    const columns = document.querySelectorAll('.waterfall-column');

    // 创建图片加载函数
    function loadImages() {
        const imagePromises = imageUrls.map(url => {

            return new Promise((resolve) => {
                const img = new Image();
                img.src = url;
                img.onload = () => resolve(img);
                img.onerror = () => resolve(null);
            });

        });

        return Promise.all(imagePromises);
    }

    // 初始化瀑布流
    async function initWaterfall() {
        const images = await loadImages();

        // 清空现有列内容
        columns.forEach(column => {
            column.innerHTML = '';
            const wrapper = document.createElement('div');
            wrapper.className = 'column-wrapper';
            column.appendChild(wrapper);
        });

        // 分配图片到各列
        images.forEach((img, index) => {
            if (!img) return;

            const columnIndex = index % columns.length;
            const wrapper = columns[columnIndex].querySelector('.column-wrapper');

            // 创建图片容器
            const imgContainer = document.createElement('div');
            imgContainer.className = 'img-container';

            // 克隆图片元素并添加到容器中
            const imgClone = img.cloneNode();
            imgContainer.appendChild(imgClone);

            wrapper.appendChild(imgContainer);
        });

        // 复制内容以创建无缝滚动效果
        columns.forEach(column => {
            const wrapper = column.querySelector('.column-wrapper');
            const clone = wrapper.cloneNode(true);
            wrapper.parentNode.appendChild(clone);
        });

    }

    // 初始化瀑布流
    initWaterfall();
});




