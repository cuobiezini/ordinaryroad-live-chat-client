package tech.ordinaryroad.live.chat.client.example.client.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveEntryHistory;
import tech.ordinaryroad.live.chat.client.example.client.service.ApiKeyAuthService;
import tech.ordinaryroad.live.chat.client.example.client.service.LiveEntryHistoryService;

import java.util.HashMap;
import java.util.Map;

/**
 * 外部 API 控制器
 * 提供经过认证和限流的对外接口
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@RestController
@RequestMapping("/api/external")
public class ExternalApiController {

    @Autowired
    private LiveEntryHistoryService entryHistoryService;

    @Autowired
    private ApiKeyAuthService apiKeyAuthService;

    /**
     * 取数功能：获取一个未使用的抖音号并标记为已使用
     * 
     * <p>请求示例：</p>
     * <pre>
     * GET /api/external/fetch-display-id?apiKey=your-api-key-here&platform=douyin
     * GET /api/external/fetch-display-id?apiKey=your-api-key-here&platform=kuaishou
     * </pre>
     *
     * @param request HTTP 请求
     * @param apiKey API Key（从 URL 参数获取）
     * @param platform 平台标识（可选，默认为 douyin）
     * @return 返回一条未使用的入场记录
     */
    @GetMapping("/fetch-display-id")
    public Map<String, Object> fetchDisplayId(HttpServletRequest request, 
                                               @RequestParam String apiKey,
                                               @RequestParam(required = false) String platform) {
        // 1. 验证 API Key
        if (!apiKeyAuthService.validateApiKey(apiKey, request)) {
            log.warn("外部 API 调用认证失败: ip={}, apiKey={}", getClientIp(request), apiKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("code", 401);
            result.put("message", "认证失败：API Key 无效或已被禁用");
            return result;
        }

        // 2. 获取平台参数
        if (platform == null || platform.isEmpty()) {
            platform = "douyin"; // 默认抖音
        }

        log.info("外部 API 调用: fetch-display-id, platform={}, apiKey={}, ip={}", 
                platform, apiKey, getClientIp(request));

        // 3. 执行业务逻辑
        try {
            LiveEntryHistory entry = entryHistoryService.fetchAndMarkDisplayId(platform);

            Map<String, Object> result = new HashMap<>();
            if (entry != null) {
                result.put("success", true);
                result.put("code", 200);
                result.put("message", "成功获取账号");
                result.put("data", Map.of(
                        "displayId", entry.getDisplayId()
                ));

                log.info("外部 API 调用成功: apiKey={}, displayId={}, uid={}", 
                        apiKey, entry.getDisplayId(), entry.getUid());
            } else {
                result.put("success", false);
                result.put("code", 404);
                result.put("message", "没有可用的未使用记录");
                
                log.warn("外部 API 调用无数据: apiKey={}, platform={}", apiKey, platform);
            }

            return result;
        } catch (Exception e) {
            log.error("外部 API 调用异常: apiKey={}, platform={}", apiKey, platform, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("code", 500);
            result.put("message", "服务器内部错误：" + e.getMessage());
            return result;
        }
    }

    /**
     * 健康检查接口（无需认证）
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("message", "服务正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取客户端 IP
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            }
            return ip;
        }
        
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        return request.getRemoteAddr();
    }
}