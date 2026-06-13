package tech.ordinaryroad.live.chat.client.example.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 后台管理页面控制器
 * 提供Web管理界面的访问入口
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@Controller
public class AdminPageController {

    /**
     * 总览首页
     *
     * @return 总览页面模板
     */
    @GetMapping("/")
    public String indexPage() {
        log.info("访问总览首页");
        return "index";
    }

    /**
     * B站配置页面
     *
     * @return B站配置页面模板
     */
    @GetMapping("/bilibili")
    public String bilibiliPage() {
        log.info("访问B站配置页面");
        return "bilibili";
    }

    /**
     * 斗鱼配置页面
     *
     * @return 斗鱼配置页面模板
     */
    @GetMapping("/douyu")
    public String douyuPage() {
        log.info("访问斗鱼配置页面");
        return "douyu";
    }

    /**
     * 快手配置页面
     *
     * @return 快手配置页面模板
     */
    @GetMapping("/kuaishou")
    public String kuaishouPage() {
        log.info("访问快手配置页面");
        return "kuaishou";
    }

    /**
     * 抖音配置页面
     *
     * @return 抖音配置页面模板
     */
    @GetMapping("/douyin")
    public String douyinPage() {
        log.info("访问抖音配置页面");
        return "douyin";
    }

    /**
     * 入场记录管理页面
     *
     * @return 入场记录管理页面模板
     */
    @GetMapping("/entry-history")
    public String entryHistoryPage() {
        log.info("访问入场记录管理页面");
        return "entry-history";
    }
}
