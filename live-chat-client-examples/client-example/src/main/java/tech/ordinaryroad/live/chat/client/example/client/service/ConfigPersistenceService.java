package tech.ordinaryroad.live.chat.client.example.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformConfig;
import tech.ordinaryroad.live.chat.client.example.client.repository.PlatformConfigRepository;

import java.util.List;
import java.util.Optional;

/**
 * 配置持久化服务
 * 负责配置的数据库存取操作
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Slf4j
@Service
public class ConfigPersistenceService {

    @Autowired
    private PlatformConfigRepository platformConfigRepository;

    /**
     * 保存或更新平台配置
     *
     * @param config 平台配置
     * @return 保存后的配置
     */
    public PlatformConfig saveConfig(PlatformConfig config) {
        log.info("保存平台配置: platform={}, roomId={}", config.getPlatform(), config.getRoomId());
        return platformConfigRepository.save(config);
    }

    /**
     * 根据平台标识查询配置
     *
     * @param platform 平台标识
     * @return 平台配置（Optional）
     */
    public Optional<PlatformConfig> getConfigByPlatform(String platform) {
        return platformConfigRepository.findByPlatform(platform);
    }

    /**
     * 获取所有平台配置
     *
     * @return 配置列表
     */
    public List<PlatformConfig> getAllConfigs() {
        return platformConfigRepository.findAll();
    }

    /**
     * 检查平台配置是否存在
     *
     * @param platform 平台标识
     * @return 是否存在
     */
    public boolean existsByPlatform(String platform) {
        return platformConfigRepository.existsByPlatform(platform);
    }

    /**
     * 删除平台配置
     *
     * @param platform 平台标识
     */
    public void deleteConfig(String platform) {
        log.info("删除平台配置: platform={}", platform);
        platformConfigRepository.deleteByPlatform(platform);
    }

    /**
     * 初始化默认配置（如果不存在）
     */
    public void initializeDefaultConfigs() {
        String[] platforms = {"bilibili", "douyu", "kuaishou", "douyin"};
        
        for (String platform : platforms) {
            if (!existsByPlatform(platform)) {
                PlatformConfig defaultConfig = new PlatformConfig();
                defaultConfig.setPlatform(platform);
                defaultConfig.setRoomId("");
                defaultConfig.setCookie("");
                defaultConfig.setAutoReconnect(true);
                defaultConfig.setEnabled(true);
                
                if ("kuaishou".equals(platform)) {
                    defaultConfig.setRoomInfoGetType("NOT_COOKIE");
                }
                
                saveConfig(defaultConfig);
                log.info("初始化默认配置: platform={}", platform);
            }
        }
    }
}
