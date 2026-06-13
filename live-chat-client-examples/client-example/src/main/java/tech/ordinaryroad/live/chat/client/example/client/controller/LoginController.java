package tech.ordinaryroad.live.chat.client.example.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 登录页面控制器
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@Controller
public class LoginController {

    /**
     * 登录页面
     *
     * @return 登录页面模板
     */
    @GetMapping("/login")
    public String loginPage() {
        log.info("访问登录页面");
        return "login";
    }
}
