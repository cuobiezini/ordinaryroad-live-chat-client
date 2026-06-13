package tech.ordinaryroad.live.chat.client.example.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 平台展示配置实体类
 * 用于控制哪些平台在前端页面中显示
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "platform_display_config")
public class PlatformDisplayConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 平台标识：bilibili, douyu, kuaishou, douyin
     */
    @Column(nullable = false, unique = true, length = 50)
    private String platform;

    /**
     * 平台中文名称
     */
    @Column(nullable = false, length = 50)
    private String platformName;

    /**
     * 是否启用显示
     */
    @Column(nullable = false)
    private Boolean enabled = false;

    /**
     * 显示顺序
     */
    @Column(nullable = false)
    private Integer displayOrder = 0;

    /**
     * 平台图标emoji
     */
    @Column(length = 10)
    private String icon;

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
