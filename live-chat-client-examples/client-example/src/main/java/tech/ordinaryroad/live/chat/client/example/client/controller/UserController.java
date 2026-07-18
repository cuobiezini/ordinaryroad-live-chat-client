package tech.ordinaryroad.live.chat.client.example.client.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户认证相关信息查询
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/user/current")
    public Map<String, Object> getCurrentUser() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> result = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            result.put("username", authentication.getName());
            result.put("authenticated", true);
        } else {
            result.put("authenticated", false);
        }
        return result;
    }
}
