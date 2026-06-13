package tech.ordinaryroad.live.chat.client.example.client.model;

import lombok.Data;

/**
 * 平台配置更新请求DTO
 * 用于接收API更新配置的请求参数
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Data
public class PlatformConfigUpdateRequest {

    /**
     * 直播间ID
     */
    private String roomId;

    /**
     * Cookie认证信息
     */
    private String cookie;

    /**
     * 是否自动重连
     */
    private Boolean autoReconnect;

    /**
     * 房间信息获取方式（快手专用）：COOKIE, NOT_COOKIE
     */
    private String roomInfoGetType;

    /**
     * 是否启用该配置
     */
    private Boolean enabled;

    /**
     * 备注说明
     */
    private String remark;
}
