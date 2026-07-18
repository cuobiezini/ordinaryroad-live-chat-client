/**
 * 快手页面管理器 - 请求状态管理
 * 提供防重提交、loading 状态等功能
 */

const KuaishouRequestManager = {
    pendingRequests: new Map(),
    
    /**
     * 生成请求唯一标识
     */
    generateKey(url, method, body) {
        return `${method}:${url}:${JSON.stringify(body || {})}`;
    },
    
    /**
     * 检查请求是否正在进行
     */
    isPending(key) {
        return this.pendingRequests.has(key);
    },
    
    /**
     * 开始请求
     */
    startRequest(key) {
        this.pendingRequests.set(key, Date.now());
    },
    
    /**
     * 结束请求
     */
    endRequest(key) {
        this.pendingRequests.delete(key);
    }
};

/**
 * 安全的 fetch 封装 - 自动防重提交和 loading
 */
async function safeFetch(url, options = {}, button = null) {
    const method = options.method || 'GET';
    const key = KuaishouRequestManager.generateKey(url, method, options.body);
    
    // 检查重复请求
    if (KuaishouRequestManager.isPending(key)) {
        console.warn('请求正在进行中，已拦截重复请求:', url);
        showToast('操作进行中，请勿重复点击', 'warning');
        throw new Error('DUPLICATE_REQUEST');
    }
    
    KuaishouRequestManager.startRequest(key);
    
    // 按钮禁用和 loading
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
        KuaishouRequestManager.endRequest(key);
        
        // 恢复按钮状态
        if (button) {
            button.disabled = false;
            if (button.dataset.originalText) {
                button.textContent = button.dataset.originalText;
            }
        }
    }
}