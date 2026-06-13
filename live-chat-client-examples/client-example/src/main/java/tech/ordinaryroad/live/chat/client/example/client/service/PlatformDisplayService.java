package tech.ordinaryroad.live.chat.client.example.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformDisplayConfig;
import tech.ordinaryroad.live.chat.client.example.client.repository.PlatformDisplayConfigRepository;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 平台展示配置服务
 * 管理哪些平台在前端页面中显示
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@Service
public class PlatformDisplayService {

    @Autowired
    private PlatformDisplayConfigRepository displayConfigRepository;

    /**
     * 应用启动时初始化默认配置
     */
    @PostConstruct
    public void initDefaultConfigs() {
        log.info("初始化平台展示默认配置...");
        
        // 定义所有平台的默认配置
        Map<String, PlatformInfo> defaultPlatforms = new LinkedHashMap<>();
        defaultPlatforms.put("douyin", new PlatformInfo("抖音", "⚫", 1));
        defaultPlatforms.put("bilibili", new PlatformInfo("B站", "🟣", 2));
        defaultPlatforms.put("douyu", new PlatformInfo("斗鱼", "🔴", 3));
        defaultPlatforms.put("kuaishou", new PlatformInfo("快手", "🟠", 4));
        
        for (Map.Entry<String, PlatformInfo> entry : defaultPlatforms.entrySet()) {
            String platform = entry.getKey();
            PlatformInfo info = entry.getValue();
            
            displayConfigRepository.findByPlatform(platform).ifPresentOrElse(
                config -> {
                    log.debug("平台配置已存在: {}", platform);
                },
                () -> {
                    PlatformDisplayConfig config = new PlatformDisplayConfig();
                    config.setPlatform(platform);
                    config.setPlatformName(info.getName());
                    config.setIcon(info.getIcon());
                    config.setDisplayOrder(info.getOrder());
                    // 默认只启用抖音
                    config.setEnabled("douyin".equals(platform));
                    displayConfigRepository.save(config);
                    log.info("创建默认平台配置: {} (enabled={})", platform, config.getEnabled());
                }
            );
        }
        
        log.info("平台展示默认配置初始化完成");
    }

    /**
     * 获取所有启用的平台配置
     *
     * @return 启用的平台配置列表
     */
    public List<PlatformDisplayConfig> getEnabledPlatforms() {
        return displayConfigRepository.findByEnabledTrueOrderByDisplayOrderAsc();
    }

    /**
     * 获取所有平台配置
     *
     * @return 所有平台配置列表
     */
    public List<PlatformDisplayConfig> getAllPlatforms() {
        return displayConfigRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 获取指定平台的配置
     *
     * @param platform 平台标识
     * @return 平台配置
     */
    public PlatformDisplayConfig getPlatformConfig(String platform) {
        return displayConfigRepository.findByPlatform(platform)
                .orElseThrow(() -> new RuntimeException("平台配置不存在: " + platform));
    }

    /**
     * 启用平台显示
     *
     * @param platform 平台标识
     * @return 更新后的配置
     */
    public PlatformDisplayConfig enablePlatform(String platform) {
        PlatformDisplayConfig config = getPlatformConfig(platform);
        config.setEnabled(true);
        PlatformDisplayConfig saved = displayConfigRepository.save(config);
        log.info("启用平台显示: {}", platform);
        return saved;
    }

    /**
     * 禁用平台显示
     *
     * @param platform 平台标识
     * @return 更新后的配置
     */
    public PlatformDisplayConfig disablePlatform(String platform) {
        PlatformDisplayConfig config = getPlatformConfig(platform);
        config.setEnabled(false);
        PlatformDisplayConfig saved = displayConfigRepository.save(config);
        log.info("禁用平台显示: {}", platform);
        return saved;
    }

    /**
     * 批量更新平台显示状态
     *
     * @param platformStatus 平台状态映射 {platform: enabled}
     * @return 更新后的配置列表
     */
    public List<PlatformDisplayConfig> batchUpdatePlatforms(Map<String, Boolean> platformStatus) {
        List<PlatformDisplayConfig> updatedConfigs = new ArrayList<>();
        
        for (Map.Entry<String, Boolean> entry : platformStatus.entrySet()) {
            String platform = entry.getKey();
            Boolean enabled = entry.getValue();
            
            try {
                PlatformDisplayConfig config = getPlatformConfig(platform);
                config.setEnabled(enabled);
                updatedConfigs.add(displayConfigRepository.save(config));
                log.info("更新平台显示状态: {} -> {}", platform, enabled);
            } catch (Exception e) {
                log.error("更新平台配置失败: {}", platform, e);
            }
        }
        
        return updatedConfigs;
    }

    /**
     * 检查平台是否启用
     *
     * @param platform 平台标识
     * @return 是否启用
     */
    public boolean isPlatformEnabled(String platform) {
        return displayConfigRepository.findByPlatform(platform)
                .map(PlatformDisplayConfig::getEnabled)
                .orElse(false);
    }

    /**
     * 获取启用的平台标识列表
     *
     * @return 平台标识列表
     */
    public List<String> getEnabledPlatformIds() {
        return getEnabledPlatforms().stream()
                .map(PlatformDisplayConfig::getPlatform)
                .collect(Collectors.toList());
    }

    /**
     * 内部类：平台信息
     */
    private static class PlatformInfo {
        private final String name;
        private final String icon;
        private final Integer order;

        public PlatformInfo(String name, String icon, Integer order) {
            this.name = name;
            this.icon = icon;
            this.order = order;
        }

        public String getName() {
            return name;
        }

        public String getIcon() {
            return icon;
        }

        public Integer getOrder() {
            return order;
        }
    }
}
