/**
 * 快手页面 API 服务层
 * 封装所有后端接口调用
 */

const KuaishouAPI = {
    /**
     * 保存配置
     */
    async saveConfig(config) {
        const response = await fetch('/api/live-chat/config/updateConfig/kuaishou', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(config)
        });
        
        if (!response.ok) {
            throw new Error('保存失败');
        }
        
        return response.json();
    },
    
    /**
     * 连接客户端
     */
    async connect() {
        const response = await fetch('/api/live-chat/connect/kuaishou', {
            method: 'POST'
        });
        
        if (!response.ok) {
            throw new Error('连接失败');
        }
        
        return response.json();
    },
    
    /**
     * 断开客户端
     */
    async disconnect() {
        const response = await fetch('/api/live-chat/disconnect/kuaishou', {
            method: 'POST'
        });
        
        if (!response.ok) {
            throw new Error('断开失败');
        }
        
        return response.json();
    },
    
    /**
     * 重连客户端
     */
    async reconnect() {
        const response = await fetch('/api/live-chat/reconnect/kuaishou', {
            method: 'POST'
        });
        
        if (!response.ok) {
            throw new Error('重连失败');
        }
        
        return response.json();
    },
    
    /**
     * 获取客户端状态
     */
    async getStatus() {
        const response = await fetch('/api/live-chat/status/kuaishou');
        
        if (!response.ok) {
            throw new Error('获取状态失败');
        }
        
        return response.json();
    },
    
    /**
     * 批量连接房间
     */
    async batchConnect(roomIds) {
        const response = await safeFetch('/client/multiply/batch-connect?platform=kuaishou', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(roomIds)
        });
        
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '批量连接失败');
    },
    
    /**
     * 获取批量连接状态
     */
    async getBatchStatus() {
        const response = await fetch('/client/multiply/batch-status?platform=kuaishou');
        const result = await response.json();
        
        if (result.success) {
            return result.statusList || [];
        }
        throw new Error(result.message || '获取状态失败');
    },
    
    /**
     * 断开单个房间
     */
    async disconnectRoom(roomId) {
        const response = await fetch(`/client/multiply/batch-disconnect?roomId=${roomId}&platform=kuaishou`, {
            method: 'POST'
        });
        
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '断开失败');
    },
    
    /**
     * 断开所有房间
     */
    async disconnectAllRooms() {
        const response = await fetch('/client/multiply/batch-disconnect-all?platform=kuaishou', {
            method: 'POST'
        });
        
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '断开所有房间失败');
    },
    
    /**
     * 查询入场记录
     */
    async queryEntries(params) {
        const requestBody = {
            platform: 'kuaishou',
            pageNum: params.pageNum || 1,
            pageSize: params.pageSize || 10,
            roomId: params.roomId,
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
     * 删除单条入场记录
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
     * 批量删除入场记录
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
     * 取数（获取未使用的快手号）
     */
    async fetchDisplayId() {
        const response = await safeFetch('/api/live-entry/fetch-display-id?platform=kuaishou', {
            method: 'POST'
        });
        const result = await response.json();
        if (result.success) {
            return result;
        }
        throw new Error(result.message || '取数失败');
    }
};