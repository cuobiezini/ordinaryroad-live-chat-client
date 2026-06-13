package tech.ordinaryroad.live.chat.client.example.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformConfig;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformDisplayConfig;
import tech.ordinaryroad.live.chat.client.example.client.model.PlatformConfigUpdateRequest;
import tech.ordinaryroad.live.chat.client.example.client.model.SendDanmuRequest;
import tech.ordinaryroad.live.chat.client.example.client.service.PlatformDisplayService;
import tech.ordinaryroad.live.chat.client.example.client.service.UnifiedConfigService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一直播客户端管理控制器
 * 提供REST API进行动态配置管理和客户端控制
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Slf4j
@RestController
@RequestMapping("/api/live-chat")
public class UnifiedLiveChatController {

    @Autowired
    private UnifiedConfigService unifiedConfigService;

    @Autowired
    private PlatformDisplayService platformDisplayService;

    /**
     * 获取所有平台配置
     *
     * @return 配置列表
     */
    @GetMapping("/configs")
    public List<PlatformConfig> getAllConfigs() {
        log.info("查询所有平台配置");
        return unifiedConfigService.getAllConfigs();
    }

    /**
     * 获取指定平台配置
     *
     * @param platform 平台标识（bilibili/douyu/kuaishou/douyin）
     * @return 平台配置
     */
    @GetMapping("/config/{platform}")
    public PlatformConfig getConfig(@PathVariable String platform) {
        log.info("查询平台配置: {}", platform);
        return unifiedConfigService.getConfig(platform);
    }

    /**
     * 更新平台配置
     *
     * @param platform 平台标识
     * @param request  更新请求参数
     * @return 更新后的配置
     */
    @PostMapping("/config/updateConfig/{platform}")
    public PlatformConfig updateConfig(
            @PathVariable String platform,
            @RequestBody PlatformConfigUpdateRequest request) {
        
        log.info("更新平台配置: {}", platform);
        
        return unifiedConfigService.updateConfig(
                platform, 
                request.getRoomId(), 
                request.getCookie(), 
                request.getAutoReconnect(), 
                request.getRoomInfoGetType()
        );
    }

    /**
     * 批量更新配置
     *
     * @param configs 配置列表
     * @return 更新后的配置列表
     */
    @PutMapping("/configs/batch")
    public List<PlatformConfig> batchUpdateConfigs(@RequestBody List<PlatformConfig> configs) {
        log.info("批量更新配置，数量: {}", configs.size());
        return unifiedConfigService.batchUpdateConfigs(configs);
    }

    /**
     * 删除平台配置
     *
     * @param platform 平台标识
     */
    @DeleteMapping("/config/{platform}")
    public Map<String, Object> deleteConfig(@PathVariable String platform) {
        log.info("删除平台配置: {}", platform);
        unifiedConfigService.deleteConfig(platform);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置删除成功: " + platform);
        return result;
    }

    /**
     * 连接客户端
     *
     * @param platform 平台标识
     */
    @PostMapping("/connect/{platform}")
    public Map<String, Object> connect(@PathVariable String platform) {
        log.info("连接客户端: {}", platform);
        unifiedConfigService.connectClient(platform);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "客户端连接成功: " + platform);
        return result;
    }

    /**
     * 断开客户端
     *
     * @param platform 平台标识
     */
    @PostMapping("/disconnect/{platform}")
    public Map<String, Object> disconnect(@PathVariable String platform) {
        log.info("断开客户端: {}", platform);
        unifiedConfigService.disconnectClient(platform);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "客户端断开成功: " + platform);
        return result;
    }

    /**
     * 重新连接客户端
     *
     * @param platform 平台标识
     */
    @PostMapping("/reconnect/{platform}")
    public Map<String, Object> reconnect(@PathVariable String platform) {
        log.info("重新连接客户端: {}", platform);
        unifiedConfigService.reconnectClient(platform);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "客户端重连成功: " + platform);
        return result;
    }

    /**
     * 发送弹幕
     * 
     * @deprecated 此功能已废弃，不再使用
     *
     * @param platform 平台标识
     * @param request  发送弹幕请求
     */
    @Deprecated
    @PostMapping("/send-danmu/{platform}")
    public Map<String, Object> sendDanmu(
            @PathVariable String platform,
            @RequestBody SendDanmuRequest request) {
        
        log.warn("发送弹幕功能已废弃: platform={}, message={}", platform, request.getMessage());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "发送弹幕功能已废弃，不再支持此功能");
        return result;
    }

    /**
     * 查询客户端状态
     *
     * @param platform 平台标识
     * @return 连接状态
     */
    @GetMapping("/status/{platform}")
    public Map<String, Object> getClientStatus(@PathVariable String platform) {
        boolean connected = unifiedConfigService.isClientConnected(platform);
        
        Map<String, Object> result = new HashMap<>();
        result.put("platform", platform);
        result.put("connected", connected);
        result.put("status", connected ? "CONNECTED" : "DISCONNECTED");
        return result;
    }

    /**
     * 查询所有客户端状态
     *
     * @return 所有平台的状态
     */
    @GetMapping("/status")
    public Map<String, Object> getAllClientStatus() {
        String[] platforms = {"bilibili", "douyu", "kuaishou", "douyin"};
        Map<String, Object> allStatus = new HashMap<>();
        
        for (String platform : platforms) {
            try {
                boolean connected = unifiedConfigService.isClientConnected(platform);
                Map<String, Object> status = new HashMap<>();
                status.put("connected", connected);
                status.put("status", connected ? "CONNECTED" : "DISCONNECTED");
                allStatus.put(platform, status);
            } catch (Exception e) {
                Map<String, Object> status = new HashMap<>();
                status.put("connected", false);
                status.put("status", "ERROR");
                status.put("error", e.getMessage());
                allStatus.put(platform, status);
            }
        }
        
        return allStatus;
    }

    /**
     * 初始化所有客户端
     */
    @PostMapping("/initialize")
    public Map<String, Object> initializeAllClients() {
        log.info("初始化所有客户端");
        unifiedConfigService.initializeAllClients();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "所有客户端初始化完成");
        return result;
    }

    // ==================== 平台展示控制 API ====================

    /**
     * 获取所有启用的平台配置
     *
     * @return 启用的平台配置列表
     */
    @GetMapping("/display/enabled-platforms")
    public List<PlatformDisplayConfig> getEnabledPlatforms() {
        log.info("查询启用的平台配置");
        return platformDisplayService.getEnabledPlatforms();
    }

    /**
     * 获取所有平台配置（包括启用和禁用）
     *
     * @return 所有平台配置列表
     */
    @GetMapping("/display/all-platforms")
    public List<PlatformDisplayConfig> getAllPlatforms() {
        log.info("查询所有平台配置");
        return platformDisplayService.getAllPlatforms();
    }

    /**
     * 启用平台显示
     *
     * @param platform 平台标识
     * @return 操作结果
     */
    @PostMapping("/display/enable/{platform}")
    public Map<String, Object> enablePlatform(@PathVariable String platform) {
        log.info("启用平台显示: {}", platform);
        PlatformDisplayConfig config = platformDisplayService.enablePlatform(platform);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "平台已启用: " + config.getPlatformName());
        result.put("config", config);
        return result;
    }

    /**
     * 禁用平台显示
     *
     * @param platform 平台标识
     * @return 操作结果
     */
    @PostMapping("/display/disable/{platform}")
    public Map<String, Object> disablePlatform(@PathVariable String platform) {
        log.info("禁用平台显示: {}", platform);
        PlatformDisplayConfig config = platformDisplayService.disablePlatform(platform);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "平台已禁用: " + config.getPlatformName());
        result.put("config", config);
        return result;
    }

    /**
     * 批量更新平台显示状态
     *
     * @param platformStatus 平台状态映射 {platform: enabled}
     * @return 操作结果
     */
    @PostMapping("/display/batch-update")
    public Map<String, Object> batchUpdatePlatforms(@RequestBody Map<String, Boolean> platformStatus) {
        log.info("批量更新平台显示状态: {}", platformStatus);
        List<PlatformDisplayConfig> configs = platformDisplayService.batchUpdatePlatforms(platformStatus);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量更新成功");
        result.put("configs", configs);
        return result;
    }

    /**
     * 检查平台是否启用
     *
     * @param platform 平台标识
     * @return 是否启用
     */
    @GetMapping("/display/check/{platform}")
    public Map<String, Object> checkPlatformEnabled(@PathVariable String platform) {
        boolean enabled = platformDisplayService.isPlatformEnabled(platform);
        
        Map<String, Object> result = new HashMap<>();
        result.put("platform", platform);
        result.put("enabled", enabled);
        return result;
    }

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