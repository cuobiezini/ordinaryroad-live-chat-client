package tech.ordinaryroad.live.chat.client.example.client.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 直播礼物赠送记录实体类
 * 映射数据库表 `live_gift_history`
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "live_gift_history")
public class LiveGiftHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 平台标识: douyin, kuaishou
     */
    @Column(nullable = false, length = 20)
    private String platform;

    /**
     * 直播间ID
     */
    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;

    /**
     * 赠送者UID
     */
    @Column(nullable = false, length = 100)
    private String uid;

    /**
     * 赠送者昵称
     */
    @Column(length = 100)
    private String username;

    /**
     * 礼物ID
     */
    @Column(name = "gift_id", length = 50)
    private String giftId;

    /**
     * 礼物名称
     */
    @Column(name = "gift_name", length = 50)
    private String giftName;

    /**
     * 礼物数量
     */
    @Column(name = "gift_count")
    private Integer giftCount;

    /**
     * 礼物单价 (单位通常为平台金币)
     */
    @Column(name = "gift_price")
    private Integer giftPrice;

    /**
     * 赠送时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
