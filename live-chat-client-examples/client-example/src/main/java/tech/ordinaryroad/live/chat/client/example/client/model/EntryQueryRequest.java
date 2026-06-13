package tech.ordinaryroad.live.chat.client.example.client.model;

import lombok.Data;

/**
 * 入场记录查询请求DTO
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Data
public class EntryQueryRequest {
    
    /**
     * 直播间ID
     */
    private String roomId;
    
    /**
     * 平台标识
     */
    private String platform;
    
    /**
     * 用户昵称（模糊查询）
     */
    private String username;
    
    /**
     * 开始时间（ISO格式）
     */
    private String startTime;
    
    /**
     * 结束时间（ISO格式）
     */
    private String endTime;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}
