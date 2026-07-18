/**
 * 入场记录管理器 - 通用类
 * 用于所有平台的入场记录查询、分页、批量操作
 */
class EntryRecordManager {
    constructor(platform) {
        this.platform = platform;
        this.currentPage = 1;
        this.pageSize = 10;
        this.totalPages = 1;
        this.totalCount = 0;
        this.selectedIds = new Set();
    }

    /**
     * 获取平台名称
     */
    getPlatformName() {
        const names = {
            'douyin': '抖音',
            'douyu': '斗鱼'
        };
        return names[this.platform] || this.platform;
    }

    /**
     * 搜索入场记录
     */
    async search() {
        const roomId = document.getElementById(`entry-filter-roomId-${this.platform}`)?.value || '';
        const startTime = document.getElementById(`entry-filter-startTime-${this.platform}`)?.value || '';
        const endTime = document.getElementById(`entry-filter-endTime-${this.platform}`)?.value || '';
        const displayId = document.getElementById(`entry-filter-displayId-${this.platform}`)?.value || '';

        try {
            addLog(`正在查询${this.getPlatformName()}入场记录...`, 'info');
            
            // 显示表格loading
            this.showTableLoading('加载中...');
            
            const response = await fetch('/api/live-entry/query', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    platform: this.platform,
                    roomId: roomId,
                    startTime: startTime,
                    endTime: endTime,
                    displayId: displayId,
                    page: this.currentPage,
                    pageSize: this.pageSize
                })
            });
            
            const result = await response.json();
            
            if (result.success) {
                this.renderTable(result.data);
                addLog(`查询成功，共 ${result.data.total} 条记录`, 'success');
            } else {
                showToast('查询失败: ' + result.message, 'error');
                this.clearTable();
            }
        } catch (error) {
            showToast('查询失败: ' + error.message, 'error');
            this.clearTable();
        }
    }

    /**
     * 翻页
     */
    async changePage(page) {
        if (page < 1 || page > this.totalPages) return;
        
        this.currentPage = page;
        await this.search();
    }

    /**
     * 删除单条记录
     */
    async deleteEntry(id) {
        if (!confirm('确定要删除这条记录吗？')) return;

        try {
            showToast('删除中...', 'info');
            
            const response = await fetch(`/api/live-entry/delete/${id}`, {
                method: 'DELETE'
            });
            
            const result = await response.json();
            
            if (result.success) {
                showToast('删除成功', 'success');
                await this.search(); // 重新查询
            } else {
                showToast('删除失败: ' + result.message, 'error');
            }
        } catch (error) {
            showToast('删除失败: ' + error.message, 'error');
        }
    }

    /**
     * 批量删除
     */
    async batchDelete() {
        if (this.selectedIds.size === 0) {
            showToast('请先选择要删除的记录', 'warning');
            return;
        }

        if (!confirm(`确定要删除选中的 ${this.selectedIds.size} 条记录吗？`)) return;

        try {
            showToast('批量删除中...', 'info');
            
            const response = await fetch('/api/live-entry/batch-delete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    platform: this.platform,
                    ids: Array.from(this.selectedIds)
                })
            });
            
            const result = await response.json();
            
            if (result.success) {
                showToast(`成功删除 ${result.data.deletedCount} 条记录`, 'success');
                this.selectedIds.clear();
                await this.search();
            } else {
                showToast('批量删除失败: ' + result.message, 'error');
            }
        } catch (error) {
            showToast('批量删除失败: ' + error.message, 'error');
        }
    }

    /**
     * 标记为已使用
     */
    async markAsUsed(id) {
        try {
            showToast('标记中...', 'info');
            
            const response = await fetch(`/api/live-entry/mark-used/${id}`, {
                method: 'POST'
            });
            
            const result = await response.json();
            
            if (result.success) {
                showToast('标记成功', 'success');
                await this.search();
            } else {
                showToast('标记失败: ' + result.message, 'error');
            }
        } catch (error) {
            showToast('标记失败: ' + error.message, 'error');
        }
    }

    /**
     * 批量标记为已使用
     */
    async batchMarkAsUsed() {
        if (this.selectedIds.size === 0) {
            showToast('请先选择要标记的记录', 'warning');
            return;
        }

        if (!confirm(`确定要标记选中的 ${this.selectedIds.size} 条记录为已使用吗？`)) return;

        try {
            showToast('批量标记中...', 'info');
            
            const response = await fetch('/api/live-entry/batch-mark-used', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    platform: this.platform,
                    ids: Array.from(this.selectedIds)
                })
            });
            
            const result = await response.json();
            
            if (result.success) {
                showToast(`成功标记 ${result.data.markedCount} 条记录`, 'success');
                this.selectedIds.clear();
                await this.search();
            } else {
                showToast('批量标记失败: ' + result.message, 'error');
            }
        } catch (error) {
            showToast('批量标记失败: ' + error.message, 'error');
        }
    }

    /**
     * 取数（获取未使用的抖音号）
     */
    async fetchDisplayId() {
        try {
            showToast('取数中...', 'info');
            
            const response = await fetch('/api/live-entry/fetch-unused', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    platform: this.platform
                })
            });
            
            const result = await response.json();
            
            if (result.success) {
                showToast(`取数成功: ${result.data.displayId}`, 'success');
                // 刷新表格
                await this.search();
            } else {
                showToast('取数失败: ' + result.message, 'error');
            }
        } catch (error) {
            showToast('取数失败: ' + error.message, 'error');
        }
    }

    /**
     * 渲染表格
     */
    renderTable(data) {
        const tbody = document.getElementById(`entry-table-body-${this.platform}`);
        if (!tbody) return;

        this.totalCount = data.total || 0;
        this.totalPages = data.totalPages || 1;
        this.currentPage = data.current || 1;

        // 更新统计信息
        const countElement = document.getElementById(`entry-total-count-${this.platform}`);
        if (countElement) {
            countElement.textContent = this.totalCount;
        }

        // 渲染数据行
        if (data.records && data.records.length > 0) {
            tbody.innerHTML = data.records.map(record => `
                <tr>
                    <td><input type="checkbox" class="entry-checkbox" 
                        value="${record.id}" 
                        onchange="entryManager.toggleSelection('${record.id}')"></td>
                    <td>${record.displayId || '-'}</td>
                    <td>${record.roomId || '-'}</td>
                    <td>${record.nickname || '-'}</td>
                    <td>${formatDateTime(record.entryTime)}</td>
                    <td>${record.isUsed ? '<span class="entry-status-used">已使用</span>' : '<span class="entry-status-unused">未使用</span>'}</td>
                    <td>
                        ${!record.isUsed ? `<button class="entry-btn-mark" onclick="entryManager.markAsUsed('${record.id}')">标记为已使用</button>` : ''}
                        <button class="entry-btn-delete" onclick="entryManager.deleteEntry('${record.id}')">删除</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 40px; color: #999;">
                        <p>暂无数据</p>
                    </td>
                </tr>
            `;
        }

        // 更新分页
        this.updatePagination();
    }

    /**
     * 更新分页控件
     */
    updatePagination() {
        const paginationContainer = document.getElementById(`entry-pagination-${this.platform}`);
        if (!paginationContainer) return;

        paginationContainer.innerHTML = `
            <button onclick="entryManager.changePage(1)" ${this.currentPage === 1 ? 'disabled' : ''}>首页</button>
            <button onclick="entryManager.changePage(${this.currentPage - 1})" ${this.currentPage === 1 ? 'disabled' : ''}>上一页</button>
            <span class="entry-pagination-info">第 ${this.currentPage} / ${this.totalPages} 页，共 ${this.totalCount} 条</span>
            <button onclick="entryManager.changePage(${this.currentPage + 1})" ${this.currentPage === this.totalPages ? 'disabled' : ''}>下一页</button>
            <button onclick="entryManager.changePage(${this.totalPages})" ${this.currentPage === this.totalPages ? 'disabled' : ''}>末页</button>
        `;
    }

    /**
     * 切换选中状态
     */
    toggleSelection(id) {
        if (this.selectedIds.has(id)) {
            this.selectedIds.delete(id);
        } else {
            this.selectedIds.add(id);
        }
    }

    /**
     * 显示表格loading
     */
    showTableLoading(message = '加载中...') {
        const tbody = document.getElementById(`entry-table-body-${this.platform}`);
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 40px; color: #999;">
                        <div class="loading"></div>
                        <p style="margin-top: 15px;">${message}</p>
                    </td>
                </tr>
            `;
        }
    }

    /**
     * 清空表格
     */
    clearTable() {
        const tbody = document.getElementById(`entry-table-body-${this.platform}`);
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 40px; color: #999;">
                        <p>暂无数据</p>
                    </td>
                </tr>
            `;
        }
    }
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

/**
 * 防抖函数
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}