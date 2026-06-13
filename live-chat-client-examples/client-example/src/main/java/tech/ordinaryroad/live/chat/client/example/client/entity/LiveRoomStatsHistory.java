package tech.ordinaryroad.live.chat.client.example.client.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 直播间统计数据历史实体类
 * 映射数据库表 `live_room_stats_history`
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "live_room_stats_history")
public class LiveRoomStatsHistory {

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
     * 累计点赞数
     */
    @Column(name = "liked_count")
    private Long likedCount;

    /**
     * 当前在线观看人数
     */
    @Column(name = "watching_count")
    private Integer watchingCount;

    /**
     * 累计观看人数
     */
    @Column(name = "watched_count")
    private Long watchedCount;

    /**
     * 采样时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
