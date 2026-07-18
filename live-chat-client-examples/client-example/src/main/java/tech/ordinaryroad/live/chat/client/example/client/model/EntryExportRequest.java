package tech.ordinaryroad.live.chat.client.example.client.model;

import lombok.Data;

/**
 * 入场记录导出请求DTO
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Data
public class EntryExportRequest {
    
    /**
     * 直播间ID
     */
    private String roomId;
    
    /**
     * 平台标识
     */
    private String platform;
    
    /**
     * 开始时间（ISO格式）
     */
    private String startTime;
    
    /**
     * 结束时间（ISO格式）
     */
    private String endTime;
}
