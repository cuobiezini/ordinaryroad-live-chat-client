package tech.ordinaryroad.live.chat.client.example.client.model;

import lombok.Data;

/**
 * 发送弹幕请求DTO
 * 用于接收API发送弹幕的请求参数
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Data
public class SendDanmuRequest {

    /**
     * 弹幕内容（不能为空）
     */
    private String message;
}
