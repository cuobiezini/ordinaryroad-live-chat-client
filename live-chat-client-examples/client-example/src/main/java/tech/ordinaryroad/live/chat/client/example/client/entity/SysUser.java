package tech.ordinaryroad.live.chat.client.example.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 用于后台管理系统登录认证
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(nullable = false, length = 200)
    private String password;

    /**
     * 真实姓名
     */
    @Column(length = 50)
    private String realName;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 角色（ROLE_USER, ROLE_ADMIN）
     */
    @Column(nullable = false, length = 50)
    private String role = "ROLE_ADMIN";

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

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
