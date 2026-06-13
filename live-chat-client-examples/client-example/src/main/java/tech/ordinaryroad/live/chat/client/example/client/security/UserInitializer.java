package tech.ordinaryroad.live.chat.client.example.client.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tech.ordinaryroad.live.chat.client.example.client.entity.SysUser;
import tech.ordinaryroad.live.chat.client.example.client.repository.SysUserRepository;

import jakarta.annotation.PostConstruct;

/**
 * 用户初始化服务
 * 应用启动时创建默认管理员账号
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@Component
public class UserInitializer {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 应用启动时初始化默认用户
     */
    @PostConstruct
    public void initDefaultUser() {
        log.info("检查并初始化默认管理员账号...");
        
        // 检查admin用户是否存在
        if (!userRepository.existsByUsername("admin")) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@ordinaryroad.tech");
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            admin.setRemark("默认管理员账号，请及时修改密码");
            
            userRepository.save(admin);
            log.info("✅ 默认管理员账号创建成功");
            log.info("   用户名: admin");
            log.info("   密码: admin123");
            log.info("   ⚠️  请及时修改默认密码！");
        } else {
            log.info("默认管理员账号已存在");
        }
    }
}
