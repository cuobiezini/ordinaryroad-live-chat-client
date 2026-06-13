package tech.ordinaryroad.live.chat.client.example.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformConfig;
import tech.ordinaryroad.live.chat.client.example.client.model.PlatformConfigUpdateRequest;
import tech.ordinaryroad.live.chat.client.example.client.model.SendDanmuRequest;
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
     * @param platform 平台标识
     * @param request  发送弹幕请求
     */
    @PostMapping("/send-danmu/{platform}")
    public Map<String, Object> sendDanmu(
            @PathVariable String platform,
            @RequestBody SendDanmuRequest request) {
        
        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            throw new IllegalArgumentException("弹幕内容不能为空");
        }
        
        log.info("发送弹幕: platform={}, message={}", platform, request.getMessage());
        unifiedConfigService.sendDanmu(platform, request.getMessage());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "弹幕发送成功");
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
}