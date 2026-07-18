package tech.ordinaryroad.live.chat.client.example.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformConfig;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformDisplayConfig;
import tech.ordinaryroad.live.chat.client.example.client.model.PlatformConfigUpdateRequest;

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
                request.getCookie(),
                request.getAutoReconnect(),
                request.getRoomInfoGetType()
        );
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

}