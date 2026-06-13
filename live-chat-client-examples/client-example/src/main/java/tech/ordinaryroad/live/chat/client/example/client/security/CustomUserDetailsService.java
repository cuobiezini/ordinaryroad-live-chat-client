package tech.ordinaryroad.live.chat.client.example.client.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.ordinaryroad.live.chat.client.example.client.entity.SysUser;
import tech.ordinaryroad.live.chat.client.example.client.repository.SysUserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 自定义UserDetailsService
 * 用于Spring Security认证
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("尝试加载用户: {}", username);
        
        SysUser sysUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        if (!sysUser.getEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 更新最后登录时间
        sysUser.setLastLoginTime(LocalDateTime.now());
        userRepository.save(sysUser);
        
        // 构建权限列表
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(sysUser.getRole())
        );
        
        log.info("用户登录成功: {}", username);
        
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.getEnabled(),
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                authorities
        );
    }
}
