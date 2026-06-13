package tech.ordinaryroad.live.chat.client.example.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ordinaryroad.live.chat.client.bilibili.client.BilibiliLiveChatClient;
import tech.ordinaryroad.live.chat.client.bilibili.config.BilibiliLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.bilibili.listener.IBilibiliConnectionListener;
import tech.ordinaryroad.live.chat.client.bilibili.listener.IBilibiliMsgListener;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.constant.RoomInfoGetTypeEnum;
import tech.ordinaryroad.live.chat.client.commons.base.exception.BaseException;
import tech.ordinaryroad.live.chat.client.commons.client.config.BaseLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.commons.client.enums.ClientStatusEnums;
import tech.ordinaryroad.live.chat.client.douyin.client.DouyinLiveChatClient;
import tech.ordinaryroad.live.chat.client.douyin.config.DouyinLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.douyin.listener.IDouyinConnectionListener;
import tech.ordinaryroad.live.chat.client.douyin.listener.IDouyinMsgListener;
import tech.ordinaryroad.live.chat.client.douyu.client.DouyuLiveChatClient;
import tech.ordinaryroad.live.chat.client.douyu.config.DouyuLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.douyu.listener.IDouyuConnectionListener;
import tech.ordinaryroad.live.chat.client.douyu.listener.IDouyuMsgListener;
import tech.ordinaryroad.live.chat.client.kuaishou.client.KuaishouLiveChatClient;
import tech.ordinaryroad.live.chat.client.kuaishou.config.KuaishouLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.kuaishou.listener.IKuaishouConnectionListener;
import tech.ordinaryroad.live.chat.client.kuaishou.listener.IKuaishouMsgListener;
import tech.ordinaryroad.live.chat.client.servers.netty.client.base.BaseNettyClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mjz
 * @date 2023/8/21
 */
@Slf4j
@RestController
@RequestMapping("client/multiply")
public class MultiplyLiveChatClientController {

    // 存储批量连接的房间ID和对应的客户端实例
    private final Map<Long, BaseNettyClient<?, ?, ?, ?, ?, ?, ?>> batchConnectedClients = new ConcurrentHashMap<>();

    @Autowired
    IBilibiliMsgListener bilibiliMsgListener;
    @Autowired
    IBilibiliConnectionListener bilibiliConnectionListener;
    @Autowired
    IDouyuMsgListener douyuMsgListener;
    @Autowired
    IDouyuConnectionListener douyuConnectionListener;
    @Autowired
    IKuaishouMsgListener kuaishouMsgListener;
    @Autowired
    IKuaishouConnectionListener kuaishouConnectionListener;
    @Autowired
    IDouyinMsgListener douyinMsgListener;
    @Autowired
    IDouyinConnectionListener douyinConnectionListener;




    @GetMapping("newClientAndStart/{roomId}")
    public void newClientAndStart(@PathVariable Long roomId, @RequestParam String platform) {
        BaseLiveChatClientConfig config;
        BaseNettyClient client;
        switch (platform) {
            case "bilibili" -> {
                config = BilibiliLiveChatClientConfig.builder()
                        .roomId(roomId)
                        .build();
                client = new BilibiliLiveChatClient((BilibiliLiveChatClientConfig) config, bilibiliMsgListener, bilibiliConnectionListener);
            }
            case "douyu" -> {
                config = DouyuLiveChatClientConfig.builder()
                        .roomId(roomId)
                        .build();
                client = new DouyuLiveChatClient((DouyuLiveChatClientConfig) config, douyuMsgListener, douyuConnectionListener);
            }
            case "kuaishou" -> {
                config = KuaishouLiveChatClientConfig.builder()
                        .roomId(roomId)
                        .roomInfoGetType(RoomInfoGetTypeEnum.NOT_COOKIE)
                        .build();
                client = new KuaishouLiveChatClient((KuaishouLiveChatClientConfig) config, kuaishouMsgListener, kuaishouConnectionListener);
            }
            case "douyin" -> {
                config = DouyinLiveChatClientConfig.builder()
                        .roomId(roomId)
                        .build();
                client = new DouyinLiveChatClient((DouyinLiveChatClientConfig) config, douyinMsgListener, douyinConnectionListener);
            }
            default -> throw new BaseException("暂不支持 " + platform);
        }
        
        // 将客户端存入Map以便后续查询状态
        batchConnectedClients.put(roomId, client);
        
        client.connect();
    }

    /**
     * 批量连接房间
     *
     * @param roomIds 房间ID列表
     * @param platform 平台标识
     * @return 连接结果
     */
    @PostMapping("batch-connect")
    public Map<String, Object> batchConnect(
            @RequestBody List<Long> roomIds,
            @RequestParam String platform
    ) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> failedRooms = new ArrayList<>();

        for (Long roomId : roomIds) {
            try {
                newClientAndStart(roomId, platform);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failedRooms.add(roomId.toString());
                log.error("连接房间失败: roomId={}, error={}", roomId, e.getMessage());
            }
        }

        result.put("success", true);
        result.put("total", roomIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedRooms", failedRooms);
        result.put("message", String.format("批量连接完成: 成功%d个，失败%d个", successCount, failCount));

        return result;
    }

    /**
     * 获取批量连接房间的状态
     *
     * @param platform 平台标识
     * @return 所有已连接房间的状态列表
     */
    @GetMapping("batch-status")
    public Map<String, Object> getBatchConnectStatus(@RequestParam String platform) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> statusList = new ArrayList<>();

        // 遍历所有已连接的客户端
        for (Map.Entry<Long, BaseNettyClient<?, ?, ?, ?, ?, ?, ?>> entry : batchConnectedClients.entrySet()) {
            Long roomId = entry.getKey();
            BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = entry.getValue();

            if (client != null) {
                Map<String, Object> status = new HashMap<>();
                status.put("roomId", roomId);
                status.put("platform", platform);

                try {
                    ClientStatusEnums clientStatus = client.getStatus();
                    status.put("status", clientStatus.name());
                    status.put("statusCode", clientStatus.getCode());

                    // 根据状态码判断是否已连接
                    boolean isConnected = clientStatus == ClientStatusEnums.CONNECTED ||
                            clientStatus == ClientStatusEnums.RECONNECTING;
                    status.put("connected", isConnected);

                    // 设置状态显示文本
                    String statusText = getStatusText(clientStatus);
                    status.put("statusText", statusText);

                } catch (Exception e) {
                    status.put("status", "ERROR");
                    status.put("statusCode", -1);
                    status.put("connected", false);
                    status.put("statusText", "异常");
                    status.put("error", e.getMessage());
                }

                statusList.add(status);
            }
        }

        result.put("success", true);
        result.put("count", statusList.size());
        result.put("statusList", statusList);

        return result;
    }

    /**
     * 断开指定房间的批量连接
     *
     * @param roomId 房间ID
     * @param platform 平台标识
     * @return 操作结果
     */
    @PostMapping("batch-disconnect")
    public Map<String, Object> batchDisconnect(
            @RequestParam Long roomId,
            @RequestParam String platform
    ) {
        Map<String, Object> result = new HashMap<>();

        BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = batchConnectedClients.remove(roomId);
        if (client != null) {
            try {
                client.disconnect(true); // 取消自动重连
                result.put("success", true);
                result.put("message", "房间 " + roomId + " 已断开连接");
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "断开连接失败: " + e.getMessage());
            }
        } else {
            result.put("success", false);
            result.put("message", "未找到该房间的连接记录");
        }

        return result;
    }

    /**
     * 断开所有批量连接的房间
     *
     * @param platform 平台标识
     * @return 操作结果
     */
    @PostMapping("batch-disconnect-all")
    public Map<String, Object> batchDisconnectAll(@RequestParam String platform) {
        Map<String, Object> result = new HashMap<>();
        int count = 0;

        for (Map.Entry<Long, BaseNettyClient<?, ?, ?, ?, ?, ?, ?>> entry : batchConnectedClients.entrySet()) {
            BaseNettyClient<?, ?, ?, ?, ?, ?, ?> client = entry.getValue();
            if (client != null) {
                try {
                    client.disconnect(true);
                    count++;
                } catch (Exception e) {
                    log.error("断开房间 {} 失败: {}", entry.getKey(), e.getMessage());
                }
            }
        }

        batchConnectedClients.clear();

        result.put("success", true);
        result.put("count", count);
        result.put("message", "已断开 " + count + " 个房间的连接");

        return result;
    }

    /**
     * 获取状态显示文本
     */
    private String getStatusText(ClientStatusEnums status) {
        switch (status) {
            case CONNECTED:
                return "已连接";
            case CONNECTING:
                return "连接中...";
            case RECONNECTING:
                return "重连中...";
            case DISCONNECTED:
                return "已断开";
            case CONNECT_FAILED:
                return "连接失败";
            case INITIALIZED:
                return "已初始化";
            case NEW:
                return "新建";
            case DESTROYED:
                return "已销毁";
            default:
                return "未知";
        }
    }

}
