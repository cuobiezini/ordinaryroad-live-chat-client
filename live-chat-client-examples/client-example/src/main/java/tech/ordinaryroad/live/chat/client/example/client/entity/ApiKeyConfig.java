package tech.ordinaryroad.live.chat.client.example.client.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * API Key 实体类
 * 用于外部接口调用认证
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_key_config")
public class ApiKeyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * API Key（唯一标识）
     */
    @Column(name = "api_key", nullable = false, unique = true, length = 128)
    private String apiKey;

    /**
     * API Secret（用于签名验证，可选）
     */
    @Column(name = "api_secret", length = 256)
    private String apiSecret;

    /**
     * 调用方名称/备注
     */
    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;

    /**
     * IP 白名单（多个IP用逗号分隔，为空表示不限制）
     */
    @Column(name = "ip_whitelist", columnDefinition = "TEXT")
    private String ipWhitelist;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 每秒最大请求数（QPS限流）
     */
    @Column(name = "max_qps")
    private Integer maxQps = 10;

    /**
     * 每日最大请求数
     */
    @Column(name = "max_daily_requests")
    private Integer maxDailyRequests = 10000;

    /**
     * 备注说明
     */
    @Column(columnDefinition = "TEXT")
    private String remark;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 最后使用时间
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
