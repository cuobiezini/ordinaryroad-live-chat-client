package tech.ordinaryroad.live.chat.client.example.client.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.msg.KuaishouDanmuMsg;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.msg.KuaishouGiftMsg;
import tech.ordinaryroad.live.chat.client.codec.kuaishou.msg.KuaishouRoomStatsMsg;
import tech.ordinaryroad.live.chat.client.commons.base.constant.RoomLiveStatusEnum;
import tech.ordinaryroad.live.chat.client.commons.base.msg.IMsg;
import tech.ordinaryroad.live.chat.client.kuaishou.client.KuaishouLiveChatClient;
import tech.ordinaryroad.live.chat.client.kuaishou.listener.IKuaishouMsgListener;
import tech.ordinaryroad.live.chat.client.kuaishou.netty.handler.KuaishouBinaryFrameHandler;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveDanmuHistory;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveGiftHistory;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveRoomStatsHistory;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveDanmuHistoryRepository;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveGiftHistoryRepository;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveRoomStatsHistoryRepository;

/**
 * 快手直播消息监听器实现类
 * 用于处理快手直播间接收到的各类消息，例如弹幕、礼物、房间统计等。
 * 并将弹幕、礼物和房间统计消息持久化到数据库。
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Slf4j
@Service
public class KuaishouMsgListener implements IKuaishouMsgListener {

    @Autowired
    private LiveDanmuHistoryRepository liveDanmuHistoryRepository;
    @Autowired
    private LiveRoomStatsHistoryRepository liveRoomStatsHistoryRepository;
    @Autowired
    private LiveGiftHistoryRepository liveGiftHistoryRepository;

    /**
     * 从 Spring 容器中获取快手直播客户端实例
     *
     * @return KuaishouLiveChatClient
     */
    private KuaishouLiveChatClient getKuaishouLiveChatClient() {
        return SpringUtil.getBean(KuaishouLiveChatClient.class);
    }

    /**
     * 收到弹幕消息时的回调
     *
     * @param binaryFrameHandler 处理器
     * @param msg                弹幕消息内容
     */
    @Override
    public void onDanmuMsg(KuaishouBinaryFrameHandler binaryFrameHandler, KuaishouDanmuMsg msg) {
        IKuaishouMsgListener.super.onDanmuMsg(binaryFrameHandler, msg);
        String content = msg.getContent();
        String uid = msg.getUid();
        Object roomId = binaryFrameHandler.getRoomId();
        log.info("{} 收到弹幕 {} {}({})：{}", roomId, msg.getBadgeLevel() != 0 ? msg.getBadgeLevel() + msg.getBadgeName() : "", msg.getUsername(), uid, content);

        tech.ordinaryroad.live.chat.client.codec.kuaishou.protobuf.SimpleUserInfoOuterClass.SimpleUserInfo user = msg.getMsg().getUser();
        String displayId = user.getPrincipalId();
        log.info("{}:到弹幕消息 用户：{} 快手号：{},内容:{}", roomId, msg.getUsername(), displayId, content);

        // 保存弹幕消息到数据库
        LiveDanmuHistory danmuHistory = new LiveDanmuHistory();
        danmuHistory.setPlatform("kuaishou");
        danmuHistory.setRoomId(String.valueOf(roomId));
        danmuHistory.setUid(uid);
        danmuHistory.setUsername(msg.getUsername());
        danmuHistory.setDisplayId(displayId);
        danmuHistory.setBadgeName(msg.getBadgeName());
        danmuHistory.setBadgeLevel((int) msg.getBadgeLevel()); // 显式转换为int
        danmuHistory.setContent(content);
     //   liveDanmuHistoryRepository.save(danmuHistory);

        // TODO 可以用大模型进行FAQ回复
        // String answer = content + "  的回复";
        // kuaishouLiveChatClient.sendDanmu(answer);

        // 获取当前直播间的实时存活状态
        RoomLiveStatusEnum roomLiveStatus = getKuaishouLiveChatClient().getRoomInitResult().getRoomLiveStatus();
        log.debug("直播间状态 {}", roomLiveStatus);
    }

    /**
     * 收到礼物消息时的回调
     *
     * @param binaryFrameHandler 处理器
     * @param msg                礼物消息内容
     */
    @Override
    public void onGiftMsg(KuaishouBinaryFrameHandler binaryFrameHandler, KuaishouGiftMsg msg) {
        IKuaishouMsgListener.super.onGiftMsg(binaryFrameHandler, msg);
        Object roomId = binaryFrameHandler.getRoomId();
        String uid = msg.getUid();
        String username = msg.getUsername();
        String giftName = msg.getGiftName();
        String giftId = String.valueOf(msg.getGiftId());
        Integer giftCount = msg.getGiftCount();
        Integer giftPrice = 0; // 快手礼物消息中未直接提供价格，暂时设为0

        log.info("{} 收到礼物 {} {}({}) {} {}({})x{}({})",
                roomId, msg.getBadgeLevel() != 0 ? msg.getBadgeLevel() + msg.getBadgeName() : "",
                username, uid,
                giftName, giftId, giftCount, giftPrice);

        // 保存礼物消息到数据库
        LiveGiftHistory giftHistory = new LiveGiftHistory();
        giftHistory.setPlatform("kuaishou");
        giftHistory.setRoomId(String.valueOf(roomId));
        giftHistory.setUid(uid);
        giftHistory.setUsername(username);
        giftHistory.setGiftId(giftId);
        giftHistory.setGiftName(giftName);
        giftHistory.setGiftCount(giftCount);
        giftHistory.setGiftPrice(giftPrice);
        //  liveGiftHistoryRepository.save(giftHistory);
    }

    /**
     * 收到房间统计信息（如人数、点赞数）时的回调
     *
     * @param binaryFrameHandler 处理器
     * @param msg                统计消息内容
     */
    @Override
    public void onRoomStatsMsg(KuaishouBinaryFrameHandler binaryFrameHandler, KuaishouRoomStatsMsg msg) {
        IKuaishouMsgListener.super.onRoomStatsMsg(binaryFrameHandler, msg);
        Object roomId = binaryFrameHandler.getRoomId();
        log.info("{} 统计信息 累计点赞数: {}, 当前观看人数: {}, 累计观看人数: {}", roomId, msg.getLikedCount(), msg.getWatchingCount(), msg.getWatchedCount());

        // 保存房间统计消息到数据库
        LiveRoomStatsHistory statsHistory = new LiveRoomStatsHistory();
        statsHistory.setPlatform("kuaishou");
        statsHistory.setRoomId(String.valueOf(roomId));
        statsHistory.setLikedCount(msg.getLikedCount() != null ? Long.parseLong(msg.getLikedCount()) : 0L);
        statsHistory.setWatchingCount(msg.getWatchingCount() != null ? Integer.parseInt(msg.getWatchingCount()) : 0);
        statsHistory.setWatchedCount(msg.getWatchedCount() != null ? Long.parseLong(msg.getWatchedCount()) : 0L);
        //   liveRoomStatsHistoryRepository.save(statsHistory);
    }

    /**
     * 收到所有类型消息的通用回调
     *
     * @param msg 原始消息对象
     */
    @Override
    public void onMsg(IMsg msg) {
        // KuaishouCmdMsg cmdMsg = (KuaishouCmdMsg) msg;
        // log.info("收到{}消息 {}", msg.getClass(), msg);
    }

    /**
     * 收到无法识别的 CMD 指令时的回调
     *
     * @param cmdString 指令名称
     * @param msg       原始消息对象
     */
    @Override
    public void onUnknownCmd(String cmdString, IMsg msg) {
        log.info("收到未知CMD消息 {}", cmdString);
    }
}