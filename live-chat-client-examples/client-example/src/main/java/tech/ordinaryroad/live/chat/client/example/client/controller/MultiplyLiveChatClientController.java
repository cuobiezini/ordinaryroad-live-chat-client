package tech.ordinaryroad.live.chat.client.example.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.ordinaryroad.live.chat.client.bilibili.client.BilibiliLiveChatClient;
import tech.ordinaryroad.live.chat.client.bilibili.config.BilibiliLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.bilibili.listener.IBilibiliConnectionListener;
import tech.ordinaryroad.live.chat.client.bilibili.listener.IBilibiliMsgListener;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.constant.RoomInfoGetTypeEnum;
import tech.ordinaryroad.live.chat.client.commons.base.exception.BaseException;
import tech.ordinaryroad.live.chat.client.commons.client.config.BaseLiveChatClientConfig;
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

/**
 * @author mjz
 * @date 2023/8/21
 */
@RestController
@RequestMapping("client/multiply")
public class MultiplyLiveChatClientController {

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
        client.connect();
    }

}
