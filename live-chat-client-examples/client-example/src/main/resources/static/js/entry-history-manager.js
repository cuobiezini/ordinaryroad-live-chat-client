/**
 * 入场记录管理页面 - 防重提交管理器
 */
const RequestManager = {
    pendingRequests: new Map(),

    generateKey(url, method, body) {
        return `${method}:${url}:${JSON.stringify(body || {})}`;
    },

    isPending(key) {
        return this.pendingRequests.has(key);
    },

    startRequest(key) {
        this.pendingRequests.set(key, Date.now());
    },

    endRequest(key) {
        this.pendingRequests.delete(key);
    },

    clearAll() {
        this.pendingRequests.clear();
    }
};

/**
 * 带防重提交的fetch封装
 * @param {string} url - 请求URL
 * @param {Object} options - fetch选项
 * @param {HTMLElement} button - 触发请求的按钮元素（可选）
 * @returns {Promise<Response>}
 */
async function safeFetch(url, options = {}, button = null) {
    const method = options.method || 'GET';
    const key = RequestManager.generateKey(url, method, options.body);

    if (RequestManager.isPending(key)) {
        console.warn('请求正在进行中，已拦截重复请求:', url);
        showToast('操作进行中，请勿重复点击', 'warning');
        throw new Error('DUPLICATE_REQUEST');
    }

    RequestManager.startRequest(key);

    if (button) {
        button.disabled = true;
        const originalText = button.textContent;
        button.dataset.originalText = originalText;
        button.innerHTML = '<span class="loading" style="width: 14px; height: 14px; margin-right: 5px;"></span>处理中...';
    }

    try {
        const response = await fetch(url, options);
        return response;
    } catch (error) {
        throw error;
    } finally {
        RequestManager.endRequest(key);

        if (button) {
            button.disabled = false;
            if (button.dataset.originalText) {
                button.textContent = button.dataset.originalText;
            }
        }
    }
}
