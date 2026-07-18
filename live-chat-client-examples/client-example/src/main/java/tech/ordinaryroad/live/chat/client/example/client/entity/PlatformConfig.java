package tech.ordinaryroad.live.chat.client.example.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 直播平台配置实体类
 * 用于持久化各直播平台的配置信息
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "platform_config")
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 平台标识：bilibili, douyu, kuaishou, douyin
     */
    @Column(nullable = false, unique = true, length = 50)
    private String platform;

    /**
     * Cookie认证信息
     */
    @Column(columnDefinition = "TEXT")
    private String cookie;

    /**
     * 是否自动重连
     */
    @Column(nullable = false)
    private Boolean autoReconnect = true;

    /**
     * 房间信息获取方式（快手专用）
     * 可选值：COOKIE, NOT_COOKIE
     */
    @Column(length = 50)
    private String roomInfoGetType;

    /**
     * 是否启用该配置
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 备注说明
     */
    @Column(length = 500)
    private String remark;
}