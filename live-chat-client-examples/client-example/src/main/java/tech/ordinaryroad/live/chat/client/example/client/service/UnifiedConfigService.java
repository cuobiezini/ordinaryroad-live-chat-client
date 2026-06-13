package tech.ordinaryroad.live.chat.client.example.client.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.ordinaryroad.live.chat.client.bilibili.client.BilibiliLiveChatClient;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.constant.RoomInfoGetTypeEnum;
import tech.ordinaryroad.live.chat.client.commons.base.exception.BaseException;
import tech.ordinaryroad.live.chat.client.commons.client.config.BaseLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.douyin.client.DouyinLiveChatClient;
import tech.ordinaryroad.live.chat.client.douyu.client.DouyuLiveChatClient;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformConfig;
import tech.ordinaryroad.live.chat.client.kuaishou.client.KuaishouLiveChatClient;
import tech.ordinaryroad.live.chat.client.servers.netty.client.base.BaseNettyClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一配置管理服务
 * 整合持久化服务和客户端管理，提供统一的配置操作接口
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Slf4j
@Service
public class UnifiedConfigService {

    @Autowired
    private ConfigPersistenceService persistenceService;

    @Autowired(required = false)
    private BilibiliLiveChatClient bilibiliClient;

    @Autowired(required = false)
    private DouyuLiveChatClient douyuClient;

    @Autowired(required = false)
    private KuaishouLiveChatClient kuaishouClient;

    @Autowired(required = false)
    private DouyinLiveChatClient douyinClient;

    /**
     * 获取平台客户端映射
     */
    private Map<String, BaseNettyClient<?, ?, ?, ?, ?, ?, ?>> getClientMap() {
        Map<String, BaseNettyClient<?, ?, ?, ?, ?, ?, ?>> clientMap = new HashMap<>();
        if (bilibiliClient != null) {
            clientMap.put("bilibili", bilibiliClient);
        }
        if (douyuClient != null) {
            clientMap.put("douyu", douyuClient);
        }
        if (kuaishouClient != null) {
            clientMap.put("kuaishou", kuaishouClient);
        }
        if (douyinClient != null) {
            clientMap.put("douyin", douyinClient);
        }
        return clientMap;
    }

    /**
     * 获取指定平台的配置
     *
     * @param platform 平台标识
     * @return 平台配置
     */
    public PlatformConfig getConfig(String platform) {
        return persistenceService.getConfigByPlatform(platform)
                .orElseThrow(() -> new BaseException("平台配置不存在: " + platform));
    }

    /**
     * 获取所有平台配置
     *
     * @return 配置列表
     */
    public List<PlatformConfig> getAllConfigs() {
        return persistenceService.getAllConfigs();
    }

    /**
     * 更新平台配置
     *
     * @param platform        平台标识
     * @param roomId          直播间ID（可选）
     * @param cookie          Cookie（可选）
     * @param autoReconnect   自动重连（可选）
     * @param roomInfoGetType 房间信息获取方式（可选，快手专用）
     * @return 更新后的配置
     */
    public PlatformConfig updateConfig(String platform, String roomId, String cookie,
                                       Boolean autoReconnect, String roomInfoGetType) {
        PlatformConfig config = getConfig(platform);

        // 更新字段
        if (roomId != null) config.setRoomId(roomId);
        if (cookie != null) config.setCookie(cookie);
        if (autoReconnect != null) config.setAutoReconnect(autoReconnect);
        if (roomInfoGetType != null) config.setRoomInfoGetType(roomInfoGetType);

        // 保存到数据库
        PlatformConfig savedConfig = persistenceService.saveConfig(config);

        // 同步更新运行中的客户端配置
        syncConfigToClient(platform, savedConfig);

        log.info("配置更新成功: platform={}, roomId={}", platform, savedConfig.getRoomId());
        return savedConfig;
    }

    /**
     * 批量更新配置
     *
     * @param configs 配置列表
     * @return 更新后的配置列表
     */
    public List<PlatformConfig> batchUpdateConfigs(List<PlatformConfig> configs) {
        return configs.stream()
                .map(config -> updateConfig(
                        config.getPlatform(),
                        config.getRoomId(),
                        config.getCookie(),
                        config.getAutoReconnect(),
                        config.getRoomInfoGetType()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 删除平台配置
     *
     * @param platform 平台标识
     */
    public void deleteConfig(String platform) {
        // 先断开客户端连接
        disconnectClient(platform);
        // 删除配置
        persistenceService.deleteConfig(platform);
        log.info("配置删除成功: platform={}", platform);
    }

    /**
     * 连接指定平台的客户端
     *
     * @param platform 平台标识
     */
    public void connectClient(String platform) {
        BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = getClient(platform);
        if (client != null) {
            // 先从数据库加载最新配置
            PlatformConfig config = getConfig(platform);
            syncConfigToClient(platform, config);
            client.connect();
            log.info("客户端连接成功: platform={}", platform);
        } else {
            throw new BaseException("客户端未初始化: " + platform);
        }
    }

    /**
     * 断开指定平台的客户端
     *
     * @param platform 平台标识
     */
    public void disconnectClient(String platform) {
        BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = getClient(platform);
        if (client != null) {
            client.disconnect(true);
            log.info("客户端断开连接: platform={}", platform);
        }
    }

    /**
     * 重新连接客户端（先断开再连接）
     *
     * @param platform 平台标识
     */
    public void reconnectClient(String platform) {
        disconnectClient(platform);
        try {
            Thread.sleep(1000); // 等待1秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connectClient(platform);
    }

    /**
     * 发送弹幕
     *
     * @param platform 平台标识
     * @param message  弹幕内容
     */
    public void sendDanmu(String platform, String message) {
        BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = getClient(platform);
        if (client != null) {
            client.sendDanmu(message);
            log.info("弹幕发送成功: platform={}, message={}", platform, message);
        } else {
            throw new BaseException("客户端未初始化: " + platform);
        }
    }

    /**
     * 获取客户端状态
     *
     * @param platform 平台标识
     * @return 是否已连接
     */
    public boolean isClientConnected(String platform) {
        BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = getClient(platform);
        if (client != null) {
            try {
                // 通过尝试获取配置来判断是否活跃
                client.getConfig();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 将数据库配置同步到运行中的客户端
     *
     * @param platform 平台标识
     * @param config   平台配置
     */
    private void syncConfigToClient(String platform, PlatformConfig config) {
        BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = getClient(platform);
        if (client == null) {
            log.warn("客户端未初始化，无法同步配置: platform={}", platform);
            return;
        }

        try {
            // 更新基础配置
            BaseLiveChatClientConfig clientConfig = client.getConfig();
            clientConfig.setRoomId(config.getRoomId());
            if (StringUtils.isNotBlank(config.getCookie())) {
                clientConfig.setCookie(config.getCookie());
            }
            clientConfig.setAutoReconnect(config.getAutoReconnect());
            // 特殊平台配置
            String roomInfoGetType = config.getRoomInfoGetType();
            if ("kuaishou".equals(platform) && roomInfoGetType != null) {
                KuaishouLiveChatClient kuaishouClient = (KuaishouLiveChatClient) client;
                RoomInfoGetTypeEnum getType;
                try {
                    getType = RoomInfoGetTypeEnum.valueOf(roomInfoGetType);
                } catch (IllegalArgumentException e) {
                    getType = RoomInfoGetTypeEnum.getByCode(Integer.parseInt(roomInfoGetType));
                }
                if (getType != null) {
                    kuaishouClient.getConfig().setRoomInfoGetType(getType);
                }
            }

            log.debug("配置同步到客户端: platform={}", platform);
        } catch (Exception e) {
            log.error("配置同步失败: platform={}", platform, e);
        }
    }

    /**
     * 获取指定平台的客户端
     *
     * @param platform 平台标识
     * @return 客户端实例
     */
    private BaseNettyClient<?, ?, ?, ?, ?, ?, ?> getClient(String platform) {
        return getClientMap().get(platform);
    }

    /**
     * 初始化所有客户端（从数据库加载配置）
     */
    public void initializeAllClients() {
        List<PlatformConfig> configs = getAllConfigs();
        for (PlatformConfig config : configs) {
            if (config.getEnabled()) {
                try {
                    syncConfigToClient(config.getPlatform(), config);
                    log.info("客户端初始化完成: platform={}, roomId={}",
                            config.getPlatform(), config.getRoomId());
                } catch (Exception e) {
                    log.error("客户端初始化失败: platform={}", config.getPlatform(), e);
                }
            }
        }
    }
}
