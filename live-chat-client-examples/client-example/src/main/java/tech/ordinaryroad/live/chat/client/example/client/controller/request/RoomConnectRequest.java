package tech.ordinaryroad.live.chat.client.example.client.controller.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间连接请求对象
 * 用于封装房间连接所需的参数
 *
 * @author OrdinaryRoad
 * @date 2026/06/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomConnectRequest {

    /**
     * 房间ID
     */
    private String roomId;

    /**
     * 平台标识（bilibili/douyu/kuaishou/douyin）
     */
    private String platform;

    /**
     * Cookie（可选）
     */
    private String cookie;

}