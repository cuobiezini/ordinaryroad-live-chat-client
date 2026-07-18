package tech.ordinaryroad.live.chat.client.example.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.ordinaryroad.live.chat.client.example.client.entity.ApiKeyConfig;
import tech.ordinaryroad.live.chat.client.example.client.repository.ApiKeyConfigRepository;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * API Key 认证服务
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@Service
public class ApiKeyAuthService {

    @Autowired
    private ApiKeyConfigRepository apiKeyConfigRepository;

    // 注意：Redis 限流需要额外配置 spring-boot-starter-data-redis 依赖
    // 如果没有 Redis，限流功能将不会生效，但不会影响正常调用

    private static final String RATE_LIMIT_KEY_PREFIX = "api:rate_limit:";
    private static final String DAILY_COUNT_KEY_PREFIX = "api:daily_count:";

    /**
     * 验证 API Key
     *
     * @param apiKey API Key
     * @param request HTTP 请求
     * @return 是否验证通过
     */
    public boolean validateApiKey(String apiKey, HttpServletRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API Key 为空");
            return false;
        }

        // 查询 API Key 配置
        ApiKeyConfig config = apiKeyConfigRepository.findByApiKey(apiKey).orElse(null);
        if (config == null) {
            log.warn("API Key 不存在: {}", apiKey);
            return false;
        }

        // 检查是否启用
        if (!config.getEnabled()) {
            log.warn("API Key 已禁用: {}, appName={}", apiKey, config.getAppName());
            return false;
        }

        // 检查 IP 白名单
        if (!checkIpWhitelist(config, request)) {
            log.warn("IP 不在白名单中: {}, apiKey={}", getClientIp(request), apiKey);
            return false;
        }

        // 检查限流
        if (!checkRateLimit(config)) {
            log.warn("API 调用频率超限: {}, appName={}", apiKey, config.getAppName());
            return false;
        }

        // 更新最后使用时间
        config.setLastUsedAt(LocalDateTime.now());
        apiKeyConfigRepository.save(config);

        return true;
    }

    /**
     * 检查 IP 白名单
     *
     * @param config API Key 配置
     * @param request HTTP 请求
     * @return 是否在白名单中
     */
    private boolean checkIpWhitelist(ApiKeyConfig config, HttpServletRequest request) {
        String ipWhitelist = config.getIpWhitelist();
        
        // 如果白名单为空，表示不限制
        if (ipWhitelist == null || ipWhitelist.trim().isEmpty()) {
            return true;
        }

        String clientIp = getClientIp(request);
        List<String> allowedIps = Arrays.asList(ipWhitelist.split(","));
        
        return allowedIps.stream()
                .map(String::trim)
                .anyMatch(ip -> ip.equals(clientIp));
    }

    /**
     * 检查限流（QPS 和每日配额）
     *
     * @param config API Key 配置
     * @return 是否允许调用
     */
    private boolean checkRateLimit(ApiKeyConfig config) {
        String apiKey = config.getApiKey();
        int maxQps = config.getMaxQps() != null ? config.getMaxQps() : 10;
        int maxDailyRequests = config.getMaxDailyRequests() != null ? config.getMaxDailyRequests() : 10000;

        // 检查 QPS 限流
        if (!checkQpsLimit(apiKey, maxQps)) {
            return false;
        }

        // 检查每日配额
        if (!checkDailyLimit(apiKey, maxDailyRequests)) {
            return false;
        }

        return true;
    }

    /**
     * 检查 QPS 限流
     * 注意：当前版本未实现精确的 QPS 限流（需要 Redis 支持）
     * 如果需要精确限流，请添加 spring-boot-starter-data-redis 依赖
     *
     * @param apiKey API Key
     * @param maxQps 最大 QPS
     * @return 是否允许
     */
    private boolean checkQpsLimit(String apiKey, int maxQps) {
        // 当前版本不实现精确的 QPS 限流
        // TODO: 如需启用，请添加 Redis 依赖并实现滑动窗口算法
        log.debug("QPS 限流检查跳过（Redis 未配置）: apiKey={}, maxQps={}", apiKey, maxQps);
        return true;
    }

    /**
     * 检查每日配额
     * 注意：当前版本未实现精确的每日配额统计（需要 Redis 支持）
     *
     * @param apiKey API Key
     * @param maxDailyRequests 每日最大请求数
     * @return 是否允许
     */
    private boolean checkDailyLimit(String apiKey, int maxDailyRequests) {
        // 当前版本不实现精确的每日配额统计
        // TODO: 如需启用，请添加 Redis 依赖并实现计数器
        log.debug("每日配额检查跳过（Redis 未配置）: apiKey={}, maxDailyRequests={}", apiKey, maxDailyRequests);
        return true;
    }

    /**
     * 获取客户端真实 IP
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个 IP 值，第一个为真实 IP
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
        
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 获取 API Key 配置信息（用于日志记录等）
     *
     * @param apiKey API Key
     * @return API Key 配置
     */
    public ApiKeyConfig getApiKeyConfig(String apiKey) {
        return apiKeyConfigRepository.findByApiKey(apiKey).orElse(null);
    }
}
