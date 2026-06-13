package tech.ordinaryroad.live.chat.client.example.client.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 直播弹幕记录实体类
 * 映射数据库表 `live_danmu_history`
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "live_danmu_history")
public class LiveDanmuHistory {

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
     * 用户唯一标识UID
     */
    @Column(nullable = false, length = 100)
    private String uid;

    /**
     * 用户昵称
     */
    @Column(length = 100)
    private String username;

    /**
     * 展示ID (抖音号/快手号)
     */
    @Column(name = "display_id", length = 100)
    private String displayId;

    /**
     * 粉丝勋章名称
     */
    @Column(name = "badge_name", length = 50)
    private String badgeName;

    /**
     * 粉丝勋章等级
     */
    @Column(name = "badge_level")
    private Integer badgeLevel;

    /**
     * 弹幕消息内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 采集时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
