package tech.ordinaryroad.live.chat.client.example.client.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 直播间用户入场记录实体类
 * 映射数据库表 `live_entry_history`
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "live_entry_history")
public class LiveEntryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 平台标识: douyin
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
     * 抖音号
     */
    @Column(name = "display_id", length = 100)
    private String displayId;

    /**
     * 是否已被使用过: 0-未使用, 1-已使用
     */
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    /**
     * 进入时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
