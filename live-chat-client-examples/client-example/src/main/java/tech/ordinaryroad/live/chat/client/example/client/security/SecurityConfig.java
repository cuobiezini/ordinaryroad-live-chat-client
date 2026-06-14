package tech.ordinaryroad.live.chat.client.example.client.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security配置类
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（根据需求可选）
            .csrf(csrf -> csrf.disable())
            
            // 授权配置
            .authorizeHttpRequests(auth -> auth
                // 允许访问登录页面和相关资源
                .requestMatchers("/login", "/login**", "/error").permitAll()
                
                // 允许访问静态资源
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // 允许访问API文档和健康检查
                .requestMatchers("/actuator/**").permitAll()
                
                // 允许访问外部API（通过API Key认证，不需要登录）
                .requestMatchers("/api/external/**").permitAll()
                
                // 允许访问客户端连接接口（不需要登录）
                .requestMatchers("/client/connect").permitAll()
                
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            
            // 表单登录配置
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // 登出配置
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // 会话管理
            .sessionManagement(session -> session
                .maximumSessions(1)  // 最大会话数
                .maxSessionsPreventsLogin(false)  // 达到最大会话数时是否阻止新登录
            );
        
        return http.build();
    }
}