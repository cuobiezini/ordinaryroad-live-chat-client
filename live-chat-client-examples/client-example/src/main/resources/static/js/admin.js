// 通用工具函数

// 检查平台是否启用
async function checkPlatformEnabled(platform) {
    try {
        const response = await fetch(`/api/live-chat/display/check/${platform}`);
        const data = await response.json();
        return data.enabled;
    } catch (error) {
        console.error('检查平台状态失败:', error);
        return false;
    }
}

// 如果平台未启用，跳转到首页
async function requirePlatformEnabled(platform) {
    const enabled = await checkPlatformEnabled(platform);
    if (!enabled) {
        showToast('该平台未启用，已跳转到首页', 'error');
        setTimeout(() => {
            window.location.href = '/';
        }, 1500);
        return false;
    }
    return true;
}

// 添加日志
function addLog(message, type = 'info') {
    const container = document.getElementById('log-container');
    if (!container) return;
    
    const entry = document.createElement('div');
    entry.className = `log-entry log-${type}`;
    const timestamp = new Date().toLocaleTimeString('zh-CN');
    entry.textContent = `[${timestamp}] ${message}`;
    container.insertBefore(entry, container.firstChild);
    
    // 限制日志数量
    while (container.children.length > 100) {
        container.removeChild(container.lastChild);
    }
}

// 显示提示
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease-out reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 更新状态徽章
function updateStatusBadge(platform, connected) {
    const badge = document.getElementById(`status-${platform}`);
    if (badge) {
        badge.className = `status-badge ${connected ? 'status-connected' : 'status-disconnected'}`;
        badge.textContent = connected ? '已连接' : '未连接';
    }
    
    // 更新按钮状态
    const connectBtn = document.getElementById(`btn-connect-${platform}`);
    const disconnectBtn = document.getElementById(`btn-disconnect-${platform}`);
    
    if (connectBtn) connectBtn.disabled = connected;
    if (disconnectBtn) disconnectBtn.disabled = !connected;
}

// 保存配置
async function saveConfig(platform) {
    const cookie = document.getElementById(`cookie-${platform}`).value;
    const autoReconnect = document.getElementById(`autoReconnect-${platform}`).checked;

    const data = {
        cookie: cookie,
        autoReconnect: autoReconnect
    };

    try {
        addLog(`正在保存${getPlatformName(platform)}配置...`, 'info');
        const response = await fetch(`/api/live-chat/config/updateConfig/${platform}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showToast(`${getPlatformName(platform)}配置保存成功`, 'success');
            addLog(`${getPlatformName(platform)}配置保存成功`, 'success');
        } else {
            throw new Error('保存失败');
        }
    } catch (error) {
        showToast('配置保存失败', 'error');
        addLog(`配置保存失败: ${error.message}`, 'error');
    }
}

// 连接客户端
async function connectClient(platform) {
    try {
        addLog(`正在连接${getPlatformName(platform)}...`, 'info');
        const response = await fetch(`/api/live-chat/connect/${platform}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            showToast(`${getPlatformName(platform)}连接成功`, 'success');
            addLog(`${getPlatformName(platform)}连接成功`, 'success');
            await updateClientStatus(platform);
        } else {
            throw new Error('连接失败');
        }
    } catch (error) {
        showToast('连接失败', 'error');
        addLog(`连接失败: ${error.message}`, 'error');
    }
}

// 断开客户端
async function disconnectClient(platform) {
    try {
        addLog(`正在断开${getPlatformName(platform)}...`, 'warning');
        const response = await fetch(`/api/live-chat/disconnect/${platform}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            showToast(`${getPlatformName(platform)}已断开`, 'success');
            addLog(`${getPlatformName(platform)}已断开`, 'success');
            await updateClientStatus(platform);
        } else {
            throw new Error('断开失败');
        }
    } catch (error) {
        showToast('断开失败', 'error');
        addLog(`断开失败: ${error.message}`, 'error');
    }
}

// 重连客户端
async function reconnectClient(platform) {
    try {
        addLog(`正在重连${getPlatformName(platform)}...`, 'info');
        const response = await fetch(`/api/live-chat/reconnect/${platform}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            showToast(`${getPlatformName(platform)}重连成功`, 'success');
            addLog(`${getPlatformName(platform)}重连成功`, 'success');
            await updateClientStatus(platform);
        } else {
            throw new Error('重连失败');
        }
    } catch (error) {
        showToast('重连失败', 'error');
        addLog(`重连失败: ${error.message}`, 'error');
    }
}

// 更新单个客户端状态
async function updateClientStatus(platform) {
    try {
        const response = await fetch(`/api/live-chat/status/${platform}`);
        const data = await response.json();
        updateStatusBadge(platform, data.connected);
    } catch (error) {
        console.error('更新状态失败:', error);
    }
}

// 获取平台中文名称
function getPlatformName(platform) {
    const names = {
        'douyu': '斗鱼',
        'douyin': '抖音'
    };
    return names[platform] || platform;
}

// 加载平台配置
async function loadPlatformConfig(platform) {
    try {
        const response = await fetch(`/api/live-chat/config/${platform}`);
        const config = await response.json();

        // 填充表单
        const cookieInput = document.getElementById(`cookie-${platform}`);
        const autoReconnectInput = document.getElementById(`autoReconnect-${platform}`);

        if (cookieInput && config.cookie) cookieInput.value = config.cookie;
        if (autoReconnectInput) autoReconnectInput.checked = config.autoReconnect !== false;

        // 更新状态
        await updateClientStatus(platform);

    } catch (error) {
        addLog(`加载配置失败: ${error.message}`, 'error');
    }
}

// 通用确认对话框
function showConfirm(options) {
    const modal = document.getElementById('confirmModal');
    if (!modal) return;
    
    // 设置标题
    const titleEl = document.getElementById('confirmModalTitle');
    if (titleEl) {
        titleEl.textContent = options.title || '确认操作';
    }
    
    // 设置消息
    const messageEl = document.getElementById('confirmModalMessage');
    if (messageEl) {
        messageEl.textContent = options.message || '';
    }
    
    // 设置警告
    const warningEl = document.getElementById('confirmModalWarning');
    const warningTextEl = document.getElementById('confirmModalWarningText');
    if (warningEl && warningTextEl) {
        if (options.warning) {
            warningTextEl.textContent = options.warning;
            warningEl.style.display = 'block';
        } else {
            warningEl.style.display = 'none';
        }
    }
    
    // 设置按钮类型
    const btnEl = document.getElementById('confirmModalBtn');
    if (btnEl) {
        btnEl.className = `btn btn-${options.type || 'primary'}`;
        btnEl.textContent = options.btnText || '确定';
    }
    
    // 设置头部背景色
    const headerEl = document.getElementById('confirmModalHeader');
    if (headerEl) {
        const colors = {
            'danger': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
            'warning': 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
            'info': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            'success': 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)'
        };
        headerEl.style.background = colors[options.type] || colors.info;
    }
    
    // 设置确认按钮点击事件
    btnEl.onclick = async function() {
        closeConfirmModal();
        if (typeof options.onConfirm === 'function') {
            await options.onConfirm();
        }
    };
    
    // 显示对话框
    modal.style.display = 'flex';
}

// 关闭确认对话框
function closeConfirmModal() {
    const modal = document.getElementById('confirmModal');
    if (modal) {
        modal.style.display = 'none';
    }
}