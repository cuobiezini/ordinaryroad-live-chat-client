package tech.ordinaryroad.live.chat.client.example.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.api.KuaishouApis;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.resp.InterestMaskListResponse;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.resp.KuaishouUserInfoResponse;
import tech.ordinaryroad.live.chat.client.kuaishou.client.KuaishouLiveChatClient;
import tech.ordinaryroad.live.chat.client.kuaishou.config.KuaishouLiveChatClientConfig;

import java.util.List;

/**
 * 快手兴趣掩码控制器
 * 用于获取快手直播的兴趣掩码列表数据
 */
@RestController
@RequestMapping("client/kuaishou")
public class KuaishouInterestMaskController {

    @Autowired
    KuaishouLiveChatClient kuaishouLiveChatClient;

    /**
     * 获取兴趣掩码列表
     *
     * @return 兴趣掩码列表数据
     */
    @GetMapping("interestMaskList")
    public List<InterestMaskListResponse> getInterestMaskList() {
        KuaishouLiveChatClientConfig config = kuaishouLiveChatClient.getConfig();
        String kww = config.getKww();
        String cookie = config.getCookie();
        return KuaishouApis.interestMaskListResponse(cookie, kww);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息数据
     */
    @GetMapping("userinfo")
    public KuaishouUserInfoResponse getUserInfo() {
        KuaishouLiveChatClientConfig config = kuaishouLiveChatClient.getConfig();
        String kww = config.getKww();
        String cookie = config.getCookie();
        return KuaishouApis.userInfo(cookie, kww);
    }

}