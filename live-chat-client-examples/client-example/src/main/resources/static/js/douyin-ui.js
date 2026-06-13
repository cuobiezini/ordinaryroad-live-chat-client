/**
 * 抖音页面 UI 控制器
 * 处理所有页面交互逻辑
 */

const DouyinUI = {
    // 入场记录状态
    entryState: {
        currentPage: 1,
        pageSize: 10,
        totalPages: 1,
        totalCount: 0,
        selectedIds: []
    },
    
    /**
     * 初始化
     */
    async init() {
        // 检查平台是否启用
        const enabled = await requirePlatformEnabled('douyin');
        if (!enabled) return;
        
        addLog('抖音配置页面加载完成', 'info');
        await this.loadConfig();
        await this.loadBatchStatus();
        
        // 自动查询入场记录
        setTimeout(() => this.searchEntries(), 500);
        
        // 启动自动刷新
        this.startAutoRefresh();
        
        // 页面可见性控制
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.stopAutoRefresh();
                addLog('页面已隐藏，暂停自动刷新以节省资源', 'info');
            } else {
                this.startAutoRefresh();
                addLog('页面已显示，恢复自动刷新', 'info');
                this.updateClientStatus();
                this.loadBatchStatus();
            }
        });
    },
    
    /**
     * 加载平台配置
     */
    async loadConfig() {
        try {
            const response = await fetch('/api/live-chat/config/getConfig/douyin');
            const result = await response.json();
            
            if (result.success && result.data) {
                document.getElementById('roomId-douyin').value = result.data.roomId || '';
                document.getElementById('cookie-douyin').value = result.data.cookie || '';
                document.getElementById('autoReconnect-douyin').checked = result.data.autoReconnect !== false;
            }
        } catch (error) {
            console.error('加载配置失败:', error);
        }
    },
    
    /**
     * 更新客户端状态
     */
    async updateClientStatus() {
        try {
            const status = await DouyinAPI.getStatus();
            const badge = document.getElementById('status-douyin');
            const connectBtn = document.getElementById('btn-connect-douyin');
            const disconnectBtn = document.getElementById('btn-disconnect-douyin');
            
            if (badge) {
                badge.className = `status-badge ${status.connected ? 'status-connected' : 'status-disconnected'}`;
                badge.textContent = status.connected ? '已连接' : '未连接';
            }
            
            if (connectBtn) connectBtn.disabled = status.connected;
            if (disconnectBtn) disconnectBtn.disabled = !status.connected;
        } catch (error) {
            console.error('更新状态失败:', error);
        }
    },
    
    /**
     * 保存配置
     */
    async saveConfig() {
        const config = {
            roomId: document.getElementById('roomId-douyin').value,
            cookie: document.getElementById('cookie-douyin').value,
            autoReconnect: document.getElementById('autoReconnect-douyin').checked
        };
        
        try {
            await DouyinAPI.saveConfig(config);
            showToast('配置保存成功', 'success');
            addLog('配置保存成功', 'success');
        } catch (error) {
            showToast('配置保存失败', 'error');
            addLog('配置保存失败: ' + error.message, 'error');
        }
    },
    
    /**
     * 连接客户端
     */
    async connect() {
        try {
            await DouyinAPI.connect();
            showToast('连接成功', 'success');
            addLog('连接成功', 'success');
            await this.updateClientStatus();
        } catch (error) {
            showToast('连接失败: ' + error.message, 'error');
            addLog('连接失败: ' + error.message, 'error');
        }
    },
    
    /**
     * 断开客户端
     */
    async disconnect() {
        try {
            await DouyinAPI.disconnect();
            showToast('已断开', 'success');
            addLog('已断开', 'success');
            await this.updateClientStatus();
        } catch (error) {
            showToast('断开失败: ' + error.message, 'error');
            addLog('断开失败: ' + error.message, 'error');
        }
    },
    
    /**
     * 重连客户端
     */
    async reconnect() {
        try {
            await DouyinAPI.reconnect();
            showToast('重连成功', 'success');
            addLog('重连成功', 'success');
            await this.updateClientStatus();
        } catch (error) {
            showToast('重连失败: ' + error.message, 'error');
            addLog('重连失败: ' + error.message, 'error');
        }
    },
    
    /**
     * 批量连接房间
     */
    async batchConnectRooms() {
        const textarea = document.getElementById('batch-roomIds-douyin');
        const roomIdsText = textarea.value.trim();
        
        if (!roomIdsText) {
            showToast('请输入房间ID列表', 'error');
            return;
        }
        
        // 解析房间ID
        const roomIds = roomIdsText.split('\n')
            .map(line => line.trim())
            .filter(line => line && !isNaN(Number(line)))
            .map(id => Number(id));
        
        if (roomIds.length === 0) {
            showToast('没有有效的房间ID', 'error');
            return;
        }
        
        showConfirm({
            title: '批量连接',
            message: `确定要批量连接 ${roomIds.length} 个房间吗？`,
            type: 'info',
            icon: '',
            onConfirm: async () => {
                await this.executeBatchConnect(roomIds);
            }
        });
    },
    
    /**
     * 执行批量连接
     */
    async executeBatchConnect(roomIds) {
        this.showBatchConnectLoading();
        
        try {
            const result = await DouyinAPI.batchConnect(roomIds);
            
            showToast(result.message, 'success');
            addLog(result.message, 'success');
            
            if (result.failCount > 0) {
                addLog(`失败的房间: ${result.failedRooms.join(', ')}`, 'warning');
            }
            
            // 刷新批量连接状态
            setTimeout(() => this.loadBatchStatus(), 1000);
        } catch (error) {
            showToast('批量连接失败: ' + error.message, 'error');
            addLog('批量连接失败: ' + error.message, 'error');
        } finally {
            this.hideBatchConnectLoading();
        }
    },
    
    /**
     * 显示批量连接loading
     */
    showBatchConnectLoading() {
        const card = document.querySelector('.platform-card:last-child');
        if (!card) return;
        
        const loadingDiv = document.createElement('div');
        loadingDiv.id = 'batchConnectLoading';
        loadingDiv.style.cssText = `
            position: absolute; top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(255, 255, 255, 0.9); display: flex;
            flex-direction: column; justify-content: center; align-items: center;
            z-index: 100; border-radius: 15px;
        `;
        loadingDiv.innerHTML = `
            <div class="loading"></div>
            <p style="margin-top: 15px; color: #667eea; font-weight: 500;">正在批量连接房间...</p>
        `;
        
        card.style.position = 'relative';
        card.appendChild(loadingDiv);
    },
    
    /**
     * 隐藏批量连接loading
     */
    hideBatchConnectLoading() {
        const loadingDiv = document.getElementById('batchConnectLoading');
        if (loadingDiv) {
            loadingDiv.remove();
        }
    },
    
    /**
     * 加载批量连接状态
     */
    async loadBatchStatus() {
        try {
            const statusList = await DouyinAPI.getBatchStatus();
            this.renderBatchStatus(statusList);
        } catch (error) {
            showToast('加载状态失败: ' + error.message, 'error');
            addLog('加载批量连接状态失败: ' + error.message, 'error');
        }
    },
    
    /**
     * 渲染批量连接状态
     */
    renderBatchStatus(statusList) {
        const container = document.getElementById('batch-status-container');
        const countElement = document.getElementById('batch-status-count');
        
        countElement.textContent = `共 ${statusList.length} 个房间`;
        
        if (statusList.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 40px; color: #999;">
                    <p>暂无批量连接的房间</p>
                    <p style="font-size: 0.85em; margin-top: 10px;">点击"批量连接"按钮开始连接房间</p>
                </div>
            `;
            return;
        }
        
        let html = '<div style="display: grid; gap: 10px;">';
        
        statusList.forEach(status => {
            const roomId = status.roomId;
            const isConnected = status.connected;
            const statusCode = status.statusCode;
            const statusText = status.statusText || '未知';
            
            let bgColor, textColor, icon;
            if (isConnected) {
                bgColor = '#d4edda';
                textColor = '#155724';
                icon = '✅';
            } else if (statusCode === 100 || statusCode === 101) {
                bgColor = '#fff3cd';
                textColor = '#856404';
                icon = '⏳';
            } else if (statusCode === 401) {
                bgColor = '#f8d7da';
                textColor = '#721c24';
                icon = '❌';
            } else {
                bgColor = '#e2e3e5';
                textColor = '#383d41';
                icon = '⚪';
            }
            
            html += `
                <div style="background: ${bgColor}; color: ${textColor}; padding: 12px 15px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; border-left: 4px solid ${isConnected ? '#28a745' : '#dc3545'};">
                    <div style="flex: 1;">
                        <div style="font-weight: 600; font-size: 14px; margin-bottom: 4px;">
                            ${icon} 房间ID: ${roomId}
                        </div>
                        <div style="font-size: 12px; opacity: 0.9;">
                            状态: ${statusText}
                        </div>
                    </div>
                    <button onclick="DouyinUI.disconnectRoom(${roomId})" 
                            style="background: rgba(255,255,255,0.8); border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 12px; color: #dc3545; font-weight: 500;"
                            onmouseover="this.style.background='rgba(255,255,255,1)'"
                            onmouseout="this.style.background='rgba(255,255,255,0.8)'">
                        断开
                    </button>
                </div>
            `;
        });
        
        html += '</div>';
        container.innerHTML = html;
    },
    
    /**
     * 断开单个房间
     */
    async disconnectRoom(roomId) {
        showConfirm({
            title: '断开连接',
            message: `确定要断开房间 ${roomId} 的连接吗？`,
            type: 'warning',
            icon: '',
            onConfirm: async () => {
                try {
                    await DouyinAPI.disconnectRoom(roomId);
                    showToast('断开成功', 'success');
                    addLog('断开成功', 'success');
                    await this.loadBatchStatus();
                } catch (error) {
                    showToast('断开失败: ' + error.message, 'error');
                    addLog('断开失败: ' + error.message, 'error');
                }
            }
        });
    },
    
    /**
     * 断开所有房间
     */
    async disconnectAllRooms() {
        showConfirm({
            title: '断开所有连接',
            message: '确定要断开所有批量连接的房间吗？',
            warning: '此操作将断开所有通过批量连接功能连接的房间。',
            type: 'danger',
            icon: '',
            onConfirm: async () => {
                try {
                    await DouyinAPI.disconnectAllRooms();
                    showToast('已断开所有房间', 'success');
                    addLog('已断开所有房间', 'success');
                    this.renderBatchStatus([]);
                } catch (error) {
                    showToast('断开失败: ' + error.message, 'error');
                    addLog('断开失败: ' + error.message, 'error');
                }
            }
        });
    },
    
    // ========== 入场记录管理 ==========
    
    /**
     * 搜索入场记录
     */
    async searchEntries() {
        const filters = this.getEntryFilters();
        this.showEntryTableLoading();
        
        try {
            const data = await DouyinAPI.queryEntries({
                pageNum: this.entryState.currentPage,
                pageSize: this.entryState.pageSize,
                ...filters
            });
            
            this.renderEntryTable(data.content);
            this.renderEntryPagination(data.total, data.pageNum, data.pageSize, data.totalPages);
            this.entryState.totalPages = data.totalPages;
        } catch (error) {
            if (error.message !== 'DUPLICATE_REQUEST') {
                addLog('加载数据失败: ' + error.message, 'error');
                showToast('加载数据失败', 'error');
            }
        }
    },
    
    /**
     * 获取筛选条件
     */
    getEntryFilters() {
        return {
            roomId: document.getElementById('entry-filter-roomId-douyin').value,
            startTime: document.getElementById('entry-filter-startTime-douyin').value,
            endTime: document.getElementById('entry-filter-endTime-douyin').value
        };
    },
    
    /**
     * 显示表格loading
     */
    showEntryTableLoading(message = '加载中...') {
        const tbody = document.getElementById('entry-table-body-douyin');
        tbody.innerHTML = `
            <tr>
                <td colspan="10" style="text-align: center; padding: 40px; color: #999;">
                    <div class="loading"></div>
                    <p style="margin-top: 15px;">${message}</p>
                </td>
            </tr>
        `;
    },
    
    /**
     * 渲染入场记录表格
     */
    renderEntryTable(data) {
        const tbody = document.getElementById('entry-table-body-douyin');
        const totalCountElement = document.getElementById('entry-total-count-douyin');
        
        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 40px; color: #999;">暂无入场记录</td></tr>';
            totalCountElement.textContent = '0';
            return;
        }
        
        let html = '';
        data.forEach(entry => {
            const isUsed = entry.isUsed || false;
            const usedBadge = isUsed ? 
                '<span style="background: #52c41a; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px;">✓ 已使用</span>' :
                '<span style="background: #faad14; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px;">○ 未使用</span>';
            
            const enterTime = entry.createdAt ? this.formatDateTime(entry.createdAt) : '-';
            const platformName = this.getPlatformName(entry.platform);
            
            html += `
                <tr style="border-bottom: 1px solid #e0e0e0;">
                    <td style="padding: 12px; text-align: center;"><input type="checkbox" class="entry-row-checkbox" value="${entry.id}" onchange="DouyinUI.updateSelectedEntryIds()" ${isUsed ? 'disabled' : ''}></td>
                    <td style="padding: 12px;">${entry.id}</td>
                    <td style="padding: 12px;">${platformName}</td>
                    <td style="padding: 12px;">${entry.roomId || '-'}</td>
                    <td style="padding: 12px;">${entry.username || '-'}</td>
                    <td style="padding: 12px;">${entry.displayId || '-'}</td>
                    <td style="padding: 12px;">${usedBadge}</td>
                    <td style="padding: 12px;">${enterTime}</td>
                    <td style="padding: 12px; text-align: center;">
                        ${!isUsed ? `<button onclick="DouyinUI.markEntryAsUsed(${entry.id})" style="background: linear-gradient(135deg, #28a745 0%, #218838 100%); color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 11px; margin-right: 5px;">标记为已使用</button>` : ''}
                        <button onclick="DouyinUI.deleteEntryRecord(${entry.id})" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 11px;">删除</button>
                    </td>
                </tr>
            `;
        });
        
        tbody.innerHTML = html;
        totalCountElement.textContent = `${this.entryState.totalCount}`;
        document.getElementById('selectAllEntries-douyin').checked = false;
        this.entryState.selectedIds = [];
    },
    
    /**
     * 渲染分页
     */
    renderEntryPagination(total, pageNum, pageSize, totalPages) {
        this.entryState.totalCount = total;
        this.entryState.currentPage = pageNum;
        this.entryState.totalPages = totalPages;
        
        const pageInfo = document.getElementById('entry-page-info-douyin');
        const firstBtn = document.getElementById('entry-first-btn-douyin');
        const prevBtn = document.getElementById('entry-prev-btn-douyin');
        const nextBtn = document.getElementById('entry-next-btn-douyin');
        const lastBtn = document.getElementById('entry-last-btn-douyin');
        
        pageInfo.textContent = `共 ${total} 条记录，第 ${pageNum} / ${totalPages} 页`;
        firstBtn.disabled = pageNum <= 1;
        prevBtn.disabled = pageNum <= 1;
        nextBtn.disabled = pageNum >= totalPages;
        lastBtn.disabled = pageNum >= totalPages;
    },
    
    /**
     * 切换页码
     */
    async changeEntryPage(action) {
        let newPage = this.entryState.currentPage;
        
        switch(action) {
            case 'first': newPage = 1; break;
            case 'prev': newPage = this.entryState.currentPage - 1; break;
            case 'next': newPage = this.entryState.currentPage + 1; break;
            case 'last': newPage = this.entryState.totalPages; break;
            default: return;
        }
        
        if (newPage < 1 || newPage > this.entryState.totalPages) return;
        
        this.entryState.currentPage = newPage;
        this.showEntryTableLoading('加载中...');
        
        const filters = this.getEntryFilters();
        
        try {
            const data = await DouyinAPI.queryEntries({
                pageNum: this.entryState.currentPage,
                pageSize: this.entryState.pageSize,
                ...filters
            });
            
            this.renderEntryTable(data.content);
            this.renderEntryPagination(data.total, data.pageNum, data.pageSize, data.totalPages);
        } catch (error) {
            showToast('翻页失败: ' + error.message, 'error');
        }
    },
    
    /**
     * 重置筛选条件
     */
    resetEntryFilters() {
        document.getElementById('entry-filter-roomId-douyin').value = '';
        document.getElementById('entry-filter-startTime-douyin').value = '';
        document.getElementById('entry-filter-endTime-douyin').value = '';
        showToast('筛选条件已重置', 'success');
    },
    
    /**
     * 全选/取消全选
     */
    toggleSelectAllEntries() {
        const selectAll = document.getElementById('selectAllEntries-douyin').checked;
        const checkboxes = document.querySelectorAll('.entry-row-checkbox:not(:disabled)');
        checkboxes.forEach(cb => cb.checked = selectAll);
        this.updateSelectedEntryIds();
    },
    
    /**
     * 更新选中的ID列表
     */
    updateSelectedEntryIds() {
        const checkboxes = document.querySelectorAll('.entry-row-checkbox:checked');
        this.entryState.selectedIds = Array.from(checkboxes).map(cb => parseInt(cb.value));
    },
    
    /**
     * 删除单条入场记录
     */
    async deleteEntryRecord(id) {
        showConfirm({
            title: '删除记录',
            message: `确定要删除ID为 ${id} 的入场记录吗？`,
            warning: '此操作将永久删除该记录，无法恢复！',
            type: 'danger',
            icon: '',
            onConfirm: async () => {
                try {
                    this.showEntryTableLoading('删除中...');
                    await DouyinAPI.deleteEntry(id);
                    showToast('删除成功', 'success');
                    addLog('删除成功', 'success');
                    this.searchEntries();
                } catch (error) {
                    if (error.message !== 'DUPLICATE_REQUEST') {
                        showToast('删除失败: ' + error.message, 'error');
                        addLog('删除失败: ' + error.message, 'error');
                    }
                }
            }
        });
    },
    
    /**
     * 批量删除入场记录
     */
    async batchDeleteEntries() {
        if (this.entryState.selectedIds.length === 0) {
            showToast('请先选择要删除的记录', 'error');
            return;
        }
        
        showConfirm({
            title: '批量删除',
            message: `确定要删除选中的 ${this.entryState.selectedIds.length} 条记录吗？`,
            warning: '此操作将永久删除所有选中的记录，无法恢复！',
            type: 'danger',
            icon: '',
            onConfirm: async () => {
                try {
                    this.showEntryTableLoading('批量删除中...');
                    await DouyinAPI.batchDelete(this.entryState.selectedIds);
                    showToast('批量删除成功', 'success');
                    addLog('批量删除成功', 'success');
                    document.getElementById('selectAllEntries-douyin').checked = false;
                    this.entryState.selectedIds = [];
                    this.searchEntries();
                } catch (error) {
                    if (error.message !== 'DUPLICATE_REQUEST') {
                        showToast('批量删除失败: ' + error.message, 'error');
                        addLog('批量删除失败: ' + error.message, 'error');
                    }
                }
            }
        });
    },
    
    /**
     * 标记为已使用
     */
    async markEntryAsUsed(id) {
        showConfirm({
            title: '标记为已使用',
            message: '确定要将此记录标记为已使用吗？',
            type: 'warning',
            icon: '',
            onConfirm: async () => {
                try {
                    this.showEntryTableLoading('标记中...');
                    await DouyinAPI.markAsUsed(id);
                    showToast('标记成功', 'success');
                    addLog('标记成功', 'success');
                    this.searchEntries();
                } catch (error) {
                    if (error.message !== 'DUPLICATE_REQUEST') {
                        showToast('标记失败: ' + error.message, 'error');
                        addLog('标记失败: ' + error.message, 'error');
                    }
                }
            }
        });
    },
    
    /**
     * 批量标记为已使用
     */
    async batchMarkAsUsed() {
        if (this.entryState.selectedIds.length === 0) {
            showToast('请先选择要标记的记录', 'error');
            return;
        }
        
        showConfirm({
            title: '批量标记',
            message: `确定要将选中的 ${this.entryState.selectedIds.length} 条记录标记为已使用吗？`,
            type: 'warning',
            icon: '',
            onConfirm: async () => {
                try {
                    this.showEntryTableLoading('批量标记中...');
                    await DouyinAPI.batchMarkAsUsed(this.entryState.selectedIds);
                    showToast('批量标记成功', 'success');
                    addLog('批量标记成功', 'success');
                    document.getElementById('selectAllEntries-douyin').checked = false;
                    this.entryState.selectedIds = [];
                    this.searchEntries();
                } catch (error) {
                    if (error.message !== 'DUPLICATE_REQUEST') {
                        showToast('批量标记失败: ' + error.message, 'error');
                        addLog('批量标记失败: ' + error.message, 'error');
                    }
                }
            }
        });
    },
    
    /**
     * 取数功能
     */
    async fetchDisplayId() {
        showConfirm({
            title: '取数（抖音）',
            message: '确定要获取一个未使用的抖音号吗？',
            warning: '获取后该记录将被标记为已使用，不会再次被取出。',
            type: 'info',
            icon: '',
            onConfirm: async () => {
                try {
                    this.showEntryTableLoading('取数中...');
                    const result = await DouyinAPI.fetchDisplayId();
                    
                    const displayId = result.displayId || '-';
                    const uid = result.uid || '-';
                    const username = result.username || '-';
                    
                    this.showFetchResult(displayId, uid, username);
                    showToast('成功获取抖音号', 'success');
                    addLog(`取数成功: displayId=${displayId}, uid=${uid}`, 'success');
                    this.searchEntries();
                } catch (error) {
                    if (error.message !== 'DUPLICATE_REQUEST') {
                        showToast('取数失败: ' + error.message, 'error');
                        addLog('取数失败: ' + error.message, 'error');
                    }
                }
            }
        });
    },
    
    /**
     * 显示取数结果
     */
    showFetchResult(displayId, uid, username) {
        const modal = document.createElement('div');
        modal.style.cssText = 'position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 9999; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(4px);';
        
        modal.innerHTML = `
            <div style="background: white; padding: 30px; border-radius: 16px; max-width: 500px; width: 90%; box-shadow: 0 20px 60px rgba(0,0,0,0.3); animation: modalSlideIn 0.3s ease-out;">
                <h3 style="margin-top: 0; color: #667eea; display: flex; align-items: center; gap: 10px;">✅ 取数成功</h3>
                <div style="margin: 20px 0; font-size: 14px; line-height: 1.8;">
                    <p><strong>抖音号：</strong><span style="font-size: 20px; color: #667eea; font-weight: bold; margin-left: 8px;">${displayId}</span></p>
                    <p><strong>用户UID：</strong>${uid}</p>
                    <p><strong>用户昵称：</strong>${username}</p>
                    <p style="color: #999; font-size: 12px; margin-top: 15px; padding: 10px; background: #fff3cd; border-radius: 4px;">⚠️ 该记录已被标记为已使用，不会再次被取出</p>
                </div>
                <div style="text-align: right;">
                    <button onclick="this.closest('div[style*=fixed]').remove()" class="btn btn-primary" style="padding: 10px 25px;">关闭</button>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
        modal.addEventListener('click', function(e) {
            if (e.target === modal) modal.remove();
        });
    },
    
    /**
     * 清理旧数据
     */
    async showCleanDialog() {
        showConfirm({
            title: '清理旧数据',
            message: '确定要清理所有入场记录吗？',
            warning: '此操作将永久删除所有入场记录，无法恢复！请谨慎操作。',
            type: 'danger',
            icon: '',
            onConfirm: async () => {
                try {
                    this.showEntryTableLoading('清理中...');
                    const result = await DouyinAPI.cleanAllData();
                    showToast(result.message || '清理成功', 'success');
                    addLog(result.message || '清理成功', 'success');
                    this.searchEntries();
                } catch (error) {
                    if (error.message !== 'DUPLICATE_REQUEST') {
                        showToast('清理失败: ' + error.message, 'error');
                        addLog('清理失败: ' + error.message, 'error');
                    }
                }
            }
        });
    },
    
    /**
     * 获取平台中文名称
     */
    getPlatformName(platform) {
        const names = { 
            'douyin': '抖音', 
            'kuaishou': '快手',
            'bilibili': 'B站',
            'douyu': '斗鱼',
            'huya': '虎牙'
        };
        return names[platform] || platform;
    },
    
    /**
     * 格式化日期时间
     */
    formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '-';
        
        const date = new Date(dateTimeStr);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        const second = String(date.getSeconds()).padStart(2, '0');
        
        return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
    },
    
    /**
     * 启动自动刷新
     */
    startAutoRefresh() {
        this.statusInterval = setInterval(() => this.updateClientStatus(), 30000);
        this.batchInterval = setInterval(() => this.loadBatchStatus(), 45000);
        addLog('自动刷新已启动（每30秒更新状态，每45秒更新批量状态）', 'info');
    },
    
    /**
     * 停止自动刷新
     */
    stopAutoRefresh() {
        if (this.statusInterval) {
            clearInterval(this.statusInterval);
            this.statusInterval = null;
        }
        if (this.batchInterval) {
            clearInterval(this.batchInterval);
            this.batchInterval = null;
        }
    }
};

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', () => {
    DouyinUI.init();
});

// 全局函数：显示清理对话框（供HTML直接调用）
function showCleanDialog() {
    DouyinUI.showCleanDialog();
}

// 全局函数：搜索入场记录（供HTML直接调用）
function searchEntryRecords() {
    DouyinUI.searchEntries();
}

// 全局函数：重置筛选条件（供HTML直接调用）
function resetEntryFilters() {
    DouyinUI.resetEntryFilters();
}

// 全局函数：批量连接房间（供HTML直接调用）
function batchConnectRooms() {
    DouyinUI.batchConnectRooms();
}

// 全局函数：加载批量连接状态（供HTML直接调用）
function loadBatchStatus() {
    DouyinUI.loadBatchStatus();
}

// 全局函数：断开所有房间（供HTML直接调用）
function disconnectAllRooms() {
    DouyinUI.disconnectAllRooms();
}