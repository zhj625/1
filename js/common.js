/**
 * 智慧图书馆管理系统 - 公共JS模块
 * 包含HTTP请求封装、工具函数、常量定义等
 * 供 admin.html 和 user.html 共享使用
 */

(function () {
    'use strict';

    // ==================== 全局配置 ====================
    const API_BASE_URL = 'http://localhost:8080/api';

    // 占位图片
    const PLACEHOLDER_COVER = 'https://placehold.co/400x600/e2e8f0/475569?text=No+Cover';
    const PLACEHOLDER_BANNER = 'https://placehold.co/1600x600/1e293b/ffffff?text=Library+Banner';
    const PLACEHOLDER_AVATAR = 'https://placehold.co/100x100/4f46e5/ffffff?text=U';

    // ==================== 工具函数 ====================

    /**
     * 图片加载失败处理
     * @param {Event} e - 错误事件
     * @param {string} type - 图片类型: 'cover' | 'banner' | 'avatar'
     */
    const handleImgError = (e, type = 'cover') => {
        const fallback = type === 'banner' ? PLACEHOLDER_BANNER :
            (type === 'avatar' ? PLACEHOLDER_AVATAR : PLACEHOLDER_COVER);
        if (e.target.src !== fallback) {
            e.target.src = fallback;
            e.target.onerror = null;
        }
    };

    /**
     * 时间格式化函数
     * @param {string} dateStr - ISO格式时间字符串
     * @returns {string} 格式化后的时间字符串
     */
    function formatTime(dateStr) {
        const date = new Date(dateStr);
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return '刚刚';
        if (minutes < 60) return `${minutes}分钟前`;
        if (hours < 24) return `${hours}小时前`;
        if (days < 7) return `${days}天前`;
        return dateStr.split('T')[0];
    }

    // ==================== HTTP 请求封装 ====================

    /**
     * 创建HTTP客户端
     * @param {string} tokenKey - localStorage中存储token的key
     * @param {string} userInfoKey - localStorage中存储用户信息的key
     * @returns {object} HTTP客户端对象
     */
    function createHttpClient(tokenKey, userInfoKey) {
        return {
            getToken() {
                return localStorage.getItem(tokenKey) || '';
            },

            async request(url, options = {}) {
                const token = this.getToken();
                const headers = {
                    'Content-Type': 'application/json',
                    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
                    ...options.headers
                };

                try {
                    const response = await fetch(`${API_BASE_URL}${url}`, {
                        ...options,
                        headers
                    });

                    const result = await response.json();

                    if (response.status === 401) {
                        localStorage.removeItem(tokenKey);
                        localStorage.removeItem(userInfoKey);
                        window.location.reload();
                        throw new Error('登录已过期，请重新登录');
                    }

                    if (result.code !== 0) {
                        throw new Error(result.message || '请求失败');
                    }

                    return result.data;
                } catch (error) {
                    if (error.message === 'Failed to fetch') {
                        throw new Error('网络错误，请检查后端服务是否启动');
                    }
                    throw error;
                }
            },

            get(url, params = {}) {
                const queryString = new URLSearchParams(params).toString();
                const fullUrl = queryString ? `${url}?${queryString}` : url;
                return this.request(fullUrl, { method: 'GET' });
            },

            post(url, data) {
                return this.request(url, { method: 'POST', body: JSON.stringify(data) });
            },

            put(url, data) {
                return this.request(url, { method: 'PUT', body: JSON.stringify(data) });
            },

            delete(url) {
                return this.request(url, { method: 'DELETE' });
            },

            async uploadFile(url, file) {
                const token = this.getToken();
                const formData = new FormData();
                formData.append('file', file);

                try {
                    const response = await fetch(`${API_BASE_URL}${url}`, {
                        method: 'POST',
                        headers: {
                            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
                        },
                        body: formData
                    });

                    const result = await response.json();

                    if (response.status === 401) {
                        localStorage.removeItem(tokenKey);
                        localStorage.removeItem(userInfoKey);
                        window.location.reload();
                        throw new Error('登录已过期，请重新登录');
                    }

                    if (result.code !== 0) {
                        throw new Error(result.message || '上传失败');
                    }

                    return result.data;
                } catch (error) {
                    if (error.message === 'Failed to fetch') {
                        throw new Error('网络错误，请检查后端服务是否启动');
                    }
                    throw error;
                }
            }
        };
    }

    // ==================== 导出 ====================
    // 由于使用CDN方式引入Vue,这里使用全局变量方式导出
    window.LibraryCommon = {
        API_BASE_URL,
        PLACEHOLDER_COVER,
        PLACEHOLDER_BANNER,
        PLACEHOLDER_AVATAR,
        handleImgError,
        formatTime,
        createHttpClient
    };
})();
