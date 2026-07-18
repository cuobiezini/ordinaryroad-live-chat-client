/*
 * MIT License
 *
 * Copyright (c) 2023 OrdinaryRoad
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tech.ordinaryroad.live.chat.client.example.client.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import tech.ordinaryroad.live.chat.client.bilibili.client.BilibiliLiveChatClient;
import tech.ordinaryroad.live.chat.client.bilibili.config.BilibiliLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.bilibili.listener.IBilibiliConnectionListener;
import tech.ordinaryroad.live.chat.client.bilibili.listener.IBilibiliMsgListener;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.constant.RoomInfoGetTypeEnum;
import tech.ordinaryroad.live.chat.client.douyin.client.DouyinLiveChatClient;
import tech.ordinaryroad.live.chat.client.douyin.config.DouyinLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.douyu.client.DouyuLiveChatClient;
import tech.ordinaryroad.live.chat.client.douyu.config.DouyuLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.douyu.listener.IDouyuConnectionListener;
import tech.ordinaryroad.live.chat.client.douyu.listener.IDouyuMsgListener;
import tech.ordinaryroad.live.chat.client.example.client.config.listener.DouyinConnectionListener;
import tech.ordinaryroad.live.chat.client.example.client.config.listener.DouyinMsgListener;
import tech.ordinaryroad.live.chat.client.example.client.service.ConfigPersistenceService;
import tech.ordinaryroad.live.chat.client.example.client.service.UnifiedConfigService;
import tech.ordinaryroad.live.chat.client.kuaishou.client.KuaishouLiveChatClient;
import tech.ordinaryroad.live.chat.client.kuaishou.config.KuaishouLiveChatClientConfig;
import tech.ordinaryroad.live.chat.client.kuaishou.listener.IKuaishouConnectionListener;
import tech.ordinaryroad.live.chat.client.kuaishou.listener.IKuaishouMsgListener;

/**
 * 直播客户端配置类
 * 支持从数据库动态加载配置
 *
 * @author mjz
 * @date 2023/8/21
 */
@Slf4j
@Component
public class LiveChatClientConfiguration {

    @Autowired
    IBilibiliMsgListener bilibiliSendSmsReplyMsgListener;
    @Autowired
    IBilibiliConnectionListener bilibiliConnectionListener;
    @Autowired
    IDouyuMsgListener douyuCmdMsgListener;
    @Autowired
    IDouyuConnectionListener douyuConnectionListener;
    @Autowired
    IKuaishouMsgListener kuaishouMsgListener;
    @Autowired
    IKuaishouConnectionListener kuaishouConnectionListener;
    @Autowired
    DouyinMsgListener douyinMsgListener;
    @Autowired
    DouyinConnectionListener douyinConnectionListener;
    @Autowired
    ConfigPersistenceService configPersistenceService;
    @Autowired
    @Lazy
    UnifiedConfigService unifiedConfigService;

    /**
     * 应用启动时初始化默认配置
     */
    @PostConstruct
    public void init() {
        log.info("初始化直播平台默认配置...");
        configPersistenceService.initializeDefaultConfigs();
        log.info("默认配置初始化完成");
    }

    @Bean
    public BilibiliLiveChatClient bilibiliLiveChatClient() {
        // 使用空配置创建客户端，运行时从数据库加载配置
        BilibiliLiveChatClientConfig config = BilibiliLiveChatClientConfig.builder().build();
        return new BilibiliLiveChatClient(config, bilibiliSendSmsReplyMsgListener, bilibiliConnectionListener);
    }

    @Bean
    public DouyuLiveChatClient douyuLiveChatClient() {
        // 使用空配置创建客户端，运行时从数据库加载配置
        DouyuLiveChatClientConfig config = DouyuLiveChatClientConfig.builder().build();
        return new DouyuLiveChatClient(config, douyuCmdMsgListener, douyuConnectionListener);
    }

    @Bean
    public KuaishouLiveChatClient kuaishouLiveChatClient() {
        // 使用空配置创建客户端，运行时从数据库加载配置
        // 默认使用 NOT_COOKIE 模式，避免 Cookie 相关问题
        KuaishouLiveChatClientConfig config = KuaishouLiveChatClientConfig.builder()
                .roomInfoGetType(RoomInfoGetTypeEnum.COOKIE)
                .build();
        return new KuaishouLiveChatClient(config, kuaishouMsgListener, kuaishouConnectionListener);
    }

    @Bean
    public DouyinLiveChatClient douyinLiveChatClient() {
        // 使用空配置创建客户端，运行时从数据库加载配置
        DouyinLiveChatClientConfig config = DouyinLiveChatClientConfig.builder().build();
        return new DouyinLiveChatClient(config, douyinMsgListener, douyinConnectionListener);
    }

}