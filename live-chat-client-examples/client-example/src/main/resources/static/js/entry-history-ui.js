/**
 * 入场记录管理页面 - UI控制器
 */
const EntryHistoryUI = {
    // 分页状态
    state: {
        currentPage: 1,
        pageSize: 10,
        totalPages: 0,
        selectedIds: []
    },

    /**
     * 初始化页面
     */
    async init() {
        try {
            await this.loadUserInfo();
            await this.loadStatistics();
            await this.loadEntries();
            addLog('入场记录管理页面加载完成', 'info');
        } catch (error) {
            if (error.message !== 'DUPLICATE_REQUEST') {
                console.error('页面初始化失败:', error);
                showToast('页面加载失败', 'error');
            }
        }
    },

    /**
     * 加载用户信息
     */
    async loadUserInfo() {
        const user = await EntryHistoryAPI.loadCurrentUser();
        document.getElementById('user-info').textContent = `👤 ${user.username}`;
    },

    /**
     * 加载统计数据
     */
    async loadStatistics() {
        try {
            const stats = await EntryHistoryAPI.loadStatistics();
            document.getElementById('total-count').textContent = stats.totalEntries || 0;
        } catch (error) {
            if (error.message !== 'DUPLICATE_REQUEST') {
                console.error('加载统计数据失败:', error);
            }
        }
    },

    /**
     * 加载入场记录列表
     */
    async loadEntries() {
        const filters = this.getFilters();
        
        this.showTableLoading();

        try {
            const data = await EntryHistoryAPI.queryEntries({
                pageNum: this.state.currentPage,
                pageSize: this.state.pageSize,
                ...filters
            });

            this.renderTable(data.content);
            this.renderPagination(data.total, data.pageNum, data.pageSize, data.totalPages);
            this.state.totalPages = data.totalPages;
        } catch (error) {
            if (error.message !== 'DUPLICATE_REQUEST') {
                addLog('加载数据失败: ' + error.message, 'error');
                showToast('加载数据失败', 'error');
            }
        } finally {
            this.hideTableLoading();
        }
    },

    /**
     * 获取筛选条件
     */
    getFilters() {
        return {
            roomId: document.getElementById('filter-roomId').value,
            platform: document.getElementById('filter-platform').value,
            startTime: document.getElementById('filter-startTime').value,
            endTime: document.getElementById('filter-endTime').value
        };
    },

    /**
     * 显示表格loading
     */
    showTableLoading() {
        const tbody = document.getElementById('table-body');
        tbody.innerHTML = `
            <tr>
                <td colspan="10" style="text-align: center; padding: 40px;">
                    <div class="loading"></div>
                    <p style="margin-top: 15px; color: #999;">加载中...</p>
                </td>
            </tr>
        `;
    },

    /**
     * 隐藏表格loading
     */
    hideTableLoading() {
        // loading会在renderTable中自动替换
    },

    /**
     * 渲染表格
     */
    renderTable(data) {
        const tbody = document.getElementById('table-body');
        
        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; padding: 40px;">暂无数据</td></tr>';
            return;
        }

        tbody.innerHTML = data.map(entry => this.renderTableRow(entry)).join('');
    },

    /**
     * 渲染表格行
     */
    renderTableRow(entry) {
        const isUsed = entry.isUsed || false;
        const usedBadge = isUsed ? 
            '<span style="background: #52c41a; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px;">✓ 已使用</span>' :
            '<span style="background: #faad14; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px;">未使用</span>';

        return `
            <tr>
                <td class="checkbox-cell"><input type="checkbox" class="row-checkbox" value="${entry.id}" onchange="EntryHistoryUI.updateSelectedIds()"></td>
                <td>${entry.id}</td>
                <td>${this.getPlatformName(entry.platform)}</td>
                <td>${entry.roomId}</td>
                <td>${entry.username || '-'}</td>
                <td>${entry.displayId || '-'}</td>
                <td>${usedBadge}</td>
                <td>${this.formatDateTime(entry.createdAt)}</td>
                <td>
                    ${!isUsed ? `<button class="btn btn-success btn-sm" onclick="EntryHistoryUI.handleMarkAsUsed(${entry.id})">标记为已使用</button>` : ''}
                    <button class="btn btn-danger btn-sm" onclick="EntryHistoryUI.handleDelete(${entry.id})">删除</button>
                </td>
            </tr>
        `;
    },

    /**
     * 渲染分页
     */
    renderPagination(total, pageNum, pageSize, totalPages) {
        const pagination = document.getElementById('pagination');
        
        pagination.innerHTML = `
            <span>共 ${total} 条记录，第 ${pageNum}/${totalPages} 页</span>
            <button onclick="EntryHistoryUI.changePage(1)" ${pageNum === 1 ? 'disabled' : ''}>首页</button>
            <button onclick="EntryHistoryUI.changePage(${pageNum - 1})" ${pageNum === 1 ? 'disabled' : ''}>上一页</button>
            <button onclick="EntryHistoryUI.changePage(${pageNum + 1})" ${pageNum === totalPages ? 'disabled' : ''}>下一页</button>
            <button onclick="EntryHistoryUI.changePage(${totalPages})" ${pageNum === totalPages ? 'disabled' : ''}>末页</button>
        `;
    },

    /**
     * 切换页码
     */
    changePage(page) {
        if (page < 1 || page > this.state.totalPages) return;
        this.state.currentPage = page;
        this.loadEntries();
    },

    /**
     * 搜索
     */
    searchEntries() {
        this.state.currentPage = 1;
        this.loadEntries();
    },

    /**
     * 重置筛选条件
     */
    resetFilters() {
        document.getElementById('filter-roomId').value = '';
        document.getElementById('filter-platform').value = '';
        document.getElementById('filter-startTime').value = '';
        document.getElementById('filter-endTime').value = '';
        this.state.currentPage = 1;
        this.loadEntries();
    },

    /**
     * 全选/取消全选
     */
    toggleSelectAll() {
        const selectAll = document.getElementById('selectAll').checked;
        const checkboxes = document.querySelectorAll('.row-checkbox');
        checkboxes.forEach(cb => cb.checked = selectAll);
        this.updateSelectedIds();
    },

    /**
     * 更新选中的ID列表
     */
    updateSelectedIds() {
        const checkboxes = document.querySelectorAll('.row-checkbox:checked');
        this.state.selectedIds = Array.from(checkboxes).map(cb => parseInt(cb.value));
    },

    /**
     * 处理删除单条记录
     */
    handleDelete(id) {
        showConfirm({
            title: '删除记录',
            message: '确定要删除这条记录吗？',
            warning: '此操作将永久删除该记录，无法恢复！',
            type: 'danger',
            icon: '',
            onConfirm: async () => {
                try {
                    await EntryHistoryAPI.deleteEntry(id);
                    showToast('删除成功', 'success');
                    addLog('删除入场记录: id=' + id, 'success');
                    this.loadEntries();
                    this.loadStatistics();
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
     * 处理批量删除
     */
    async batchDelete() {
        if (this.state.selectedIds.length === 0) {
            showToast('请先选择要删除的记录', 'error');
            return;
        }

        showConfirm({
            title: '批量删除',
            message: `确定要删除选中的 ${this.state.selectedIds.length} 条记录吗？`,
            warning: '此操作将永久删除所有选中的记录，无法恢复！',
            type: 'danger',
            icon: '',
            onConfirm: async () => {
                try {
                    const result = await EntryHistoryAPI.batchDelete(this.state.selectedIds);
                    showToast(result.message, 'success');
                    addLog(result.message, 'success');
                    this.loadEntries();
                    this.loadStatistics();
                    this.state.selectedIds = [];
                    document.getElementById('selectAll').checked = false;
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
     * 处理标记为已使用
     */
    handleMarkAsUsed(id) {
        showConfirm({
            title: '标记为已使用',
            message: '确定要将此记录标记为已使用吗？',
            type: 'warning',
            icon: '',
            onConfirm: async () => {
                try {
                    await EntryHistoryAPI.markAsUsed(id);
                    showToast('标记成功', 'success');
                    addLog('标记入场记录为已使用: id=' + id, 'success');
                    this.loadEntries();
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
     * 处理批量标记为已使用
     */
    async batchMarkAsUsed() {
        if (this.state.selectedIds.length === 0) {
            showToast('请先选择要标记的记录', 'error');
            return;
        }

        showConfirm({
            title: '批量标记',
            message: `确定要将选中的 ${this.state.selectedIds.length} 条记录标记为已使用吗？`,
            type: 'warning',
            icon: '',
            onConfirm: async () => {
                try {
                    const result = await EntryHistoryAPI.batchMarkAsUsed(this.state.selectedIds);
                    showToast(result.message, 'success');
                    addLog(result.message, 'success');
                    this.loadEntries();
                    this.state.selectedIds = [];
                    document.getElementById('selectAll').checked = false;
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
     * 导出CSV
     */
    async exportCsv() {
        const filters = this.getFilters();

        try {
            const response = await EntryHistoryAPI.exportCsv(filters);

            // 获取文件名
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = 'live_entry.csv';
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename="?(.+?)"?$/i);
                if (filenameMatch && filenameMatch[1]) {
                    filename = filenameMatch[1];
                }
            }

            // 下载文件
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            addLog('导出CSV文件成功', 'success');
            showToast('导出成功', 'success');
        } catch (error) {
            if (error.message !== 'DUPLICATE_REQUEST') {
                showToast('导出失败: ' + error.message, 'error');
                addLog('导出失败: ' + error.message, 'error');
            }
        }
    },

    /**
     * 显示清理对话框
     */
    showCleanDialog() {
        console.log('showCleanDialog called');
        const modal = document.getElementById('cleanDataModal');
        console.log('cleanDataModal element:', modal);
        if (modal) {
            modal.style.display = 'flex';
            
            // 更新当前总记录数
            const totalCount = document.getElementById('total-count').textContent;
            document.getElementById('modalTotalCount').textContent = totalCount + ' 条';
        } else {
            console.error('cleanDataModal element not found!');
            showToast('对话框元素未找到', 'error');
        }
    },

    /**
     * 关闭清理对话框
     */
    closeCleanModal() {
        const modal = document.getElementById('cleanDataModal');
        modal.style.display = 'none';
    },

    /**
     * 确认清理所有数据
     */
    async confirmCleanAllData() {
        try {
            addLog('开始清理所有入场记录...', 'warning');
            
            const result = await EntryHistoryAPI.cleanAllData();
            showToast(result.message, 'success');
            addLog(result.message, 'success');
            this.closeCleanModal();
            this.loadEntries();
            this.loadStatistics();
        } catch (error) {
            if (error.message !== 'DUPLICATE_REQUEST') {
                showToast('清理失败: ' + error.message, 'error');
                addLog('清理失败: ' + error.message, 'error');
            }
        }
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
                    const result = await EntryHistoryAPI.fetchDisplayId('douyin');
                    
                    // 显示结果对话框
                    this.showFetchResult(result.displayId || '-', result.uid || '-', result.username || '-');
                    
                    showToast('成功获取抖音号', 'success');
                    addLog(`取数成功: displayId=${result.displayId}, uid=${result.uid}`, 'success');
                    this.loadEntries();
                    this.loadStatistics();
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
        modal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.5); z-index: 9999;
            display: flex; justify-content: center; align-items: center;
        `;
        
        modal.innerHTML = `
            <div style="background: white; padding: 30px; border-radius: 10px; max-width: 500px; box-shadow: 0 10px 40px rgba(0,0,0,0.3);">
                <h3 style="margin-top: 0; color: #667eea;">✅ 取数成功</h3>
                <div style="margin: 20px 0; font-size: 14px; line-height: 1.8;">
                    <p><strong>抖音号：</strong><span style="font-size: 18px; color: #667eea; font-weight: bold;">${displayId}</span></p>
                    <p><strong>用户UID：</strong>${uid}</p>
                    <p><strong>用户昵称：</strong>${username}</p>
                    <p style="color: #999; font-size: 12px; margin-top: 15px;">⚠️ 该记录已被标记为已使用，不会再次被取出</p>
                </div>
                <div style="text-align: right;">
                    <button onclick="this.closest('div[style*=fixed]').remove()" class="btn btn-primary">关闭</button>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
    },

    /**
     * 辅助函数：获取平台名称
     */
    getPlatformName(platform) {
        const names = { 'douyin': '抖音', 'kuaishou': '快手' };
        return names[platform] || platform;
    },

    /**
     * 辅助函数：格式化日期时间
     */
    formatDateTime(dateTime) {
        if (!dateTime) return '-';
        return dateTime.replace('T', ' ').substring(0, 19);
    }
};

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    EntryHistoryUI.init();
});

// ESC键关闭模态框
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        EntryHistoryUI.closeCleanModal();
        closeConfirmModal();
    }
});

// 全局函数：关闭清理数据对话框（供HTML直接调用）
function closeCleanModal() {
    EntryHistoryUI.closeCleanModal();
}