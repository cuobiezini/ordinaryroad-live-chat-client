/**
 * 入场记录管理页面 - API服务层
 */
const EntryHistoryAPI = {
    /**
     * 加载当前用户信息
     */
    async loadCurrentUser() {
        const response = await safeFetch('/api/live-chat/user/current');
        if (response.ok) {
            return await response.json();
        }
        throw new Error('加载用户信息失败');
    },

    /**
     * 加载统计数据
     */
    async loadStatistics() {
        const response = await safeFetch('/api/live-entry/statistics');
        const result = await response.json();
        if (result.success) {
            return result.data;
        }
        throw new Error(result.message || '加载统计数据失败');
    },

    /**
     * 查询入场记录列表
     * @param {Object} params - 查询参数
     */
    async queryEntries(params) {
        const requestBody = {
            pageNum: params.pageNum || 1,
            pageSize: params.pageSize || 10,
            roomId: params.roomId,
            platform: params.platform,
            startTime: params.startTime,
            endTime: params.endTime
        };

        // 移除空值
        Object.keys(requestBody).forEach(key => {
            if (requestBody[key] === undefined || requestBody[key] === null || requestBody[key] === '') {
                delete requestBody[key];
            }
        });

        const response = await safeFetch('/api/live-entry/query', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        const result = await response.json();
        if (result.success) {
            // 返回完整的分页对象
            return {
                content: result.data,
                total: result.total,
                pageNum: result.pageNum,
                pageSize: result.pageSize,
                totalPages: result.totalPages
            };
        }
        throw new Error(result.message || '查询失败');
    },

    /**
     * 删除单条记录
     */
    async deleteEntry(id) {
        const response = await safeFetch(`/api/live-entry/${id}`, { method: 'DELETE' });
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '删除失败');
    },

    /**
     * 批量删除记录
     */
    async batchDelete(ids) {
        const response = await safeFetch('/api/live-entry/batch-delete', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '批量删除失败');
    },

    /**
     * 标记为已使用
     */
    async markAsUsed(id) {
        const response = await safeFetch(`/api/live-entry/${id}/mark-used`, { method: 'POST' });
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '标记失败');
    },

    /**
     * 批量标记为已使用
     */
    async batchMarkAsUsed(ids) {
        const response = await safeFetch('/api/live-entry/batch-mark-used', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '批量标记失败');
    },

    /**
     * 清理所有数据
     */
    async cleanAllData() {
        const response = await safeFetch('/api/live-entry/clean-all', { method: 'DELETE' });
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '清理失败');
    },

    /**
     * 导出CSV
     */
    async exportCsv(params) {
        const requestBody = {
            roomId: params.roomId,
            platform: params.platform,
            startTime: params.startTime,
            endTime: params.endTime
        };

        // 移除空值
        Object.keys(requestBody).forEach(key => {
            if (!requestBody[key]) {
                delete requestBody[key];
            }
        });

        const response = await safeFetch('/api/live-entry/export/csv', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error('导出失败');
        }

        return response;
    },

    /**
     * 取数（获取未使用的抖音号）
     */
    async fetchDisplayId(platform = 'douyin') {
        const response = await safeFetch(`/api/live-entry/fetch-display-id?platform=${platform}`, {
            method: 'POST'
        });
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '取数失败');
    }
};
