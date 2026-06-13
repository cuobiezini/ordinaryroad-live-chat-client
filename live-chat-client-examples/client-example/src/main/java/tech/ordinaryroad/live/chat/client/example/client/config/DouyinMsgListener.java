package tech.ordinaryroad.live.chat.client.example.client.config;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.ordinaryroad.live.chat.client.codec.douyin.constant.DouyinCmdEnum;
import tech.ordinaryroad.live.chat.client.codec.douyin.msg.*;
import tech.ordinaryroad.live.chat.client.codec.douyin.room.DouyinRoomInitResult;
import tech.ordinaryroad.live.chat.client.commons.base.constant.RoomLiveStatusEnum;
import tech.ordinaryroad.live.chat.client.commons.base.msg.ICmdMsg;
import tech.ordinaryroad.live.chat.client.commons.base.msg.IMsg;
import tech.ordinaryroad.live.chat.client.douyin.client.DouyinLiveChatClient;
import tech.ordinaryroad.live.chat.client.douyin.listener.IDouyinMsgListener;
import tech.ordinaryroad.live.chat.client.douyin.netty.handler.DouyinBinaryFrameHandler;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveDanmuHistory;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveEntryHistory;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveGiftHistory;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveRoomStatsHistory;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveDanmuHistoryRepository;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveEntryHistoryRepository;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveGiftHistoryRepository;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveRoomStatsHistoryRepository;

/**
 * 抖音直播消息监听器实现类
 * 用于处理抖音直播间接收到的各类消息，例如弹幕、礼物、用户入场等。
 * 并将弹幕消息持久化到数据库。
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Slf4j
@Service
public class DouyinMsgListener implements IDouyinMsgListener {

    @Autowired
    private LiveDanmuHistoryRepository liveDanmuHistoryRepository;
    @Autowired
    private LiveEntryHistoryRepository liveEntryHistoryRepository;
    @Autowired
    private LiveRoomStatsHistoryRepository liveRoomStatsHistoryRepository;
    @Autowired
    private LiveGiftHistoryRepository liveGiftHistoryRepository;

    /**
     * 获取抖音直播客户端实例
     *
     * @return 抖音直播客户端
     */
    private DouyinLiveChatClient getDouyinLiveChatClient() {
        return SpringUtil.getBean(DouyinLiveChatClient.class);
    }

    /**
     * 处理接收到的弹幕消息
     *
     * @param binaryFrameHandler 二进制帧处理器
     * @param msg                弹幕消息体
     */
    @Override
    public void onDanmuMsg(DouyinBinaryFrameHandler binaryFrameHandler, DouyinDanmuMsg msg) {
        IDouyinMsgListener.super.onDanmuMsg(binaryFrameHandler, msg);
        String content = msg.getContent();
        String uid = msg.getUid();
        Object roomId = binaryFrameHandler.getRoomId();
        log.debug("{} 收到弹幕 {} {}({})：{}", roomId, msg.getBadgeLevel() != 0 ? msg.getBadgeLevel() + msg.getBadgeName() : "", msg.getUsername(), uid, content);
        tech.ordinaryroad.live.chat.client.codec.douyin.protobuf.User user = msg.getMsg().getUser();
        String displayId = user.getDisplayId(); // 抖音号 (最常用)
        log.info("用户 :{} content ：{} 抖音号：{}", msg.getUsername(), content, displayId);

        // 保存弹幕消息到数据库
        LiveDanmuHistory danmuHistory = new LiveDanmuHistory();
        danmuHistory.setPlatform("douyin");
        danmuHistory.setRoomId(String.valueOf(roomId));
        danmuHistory.setUid(uid);
        danmuHistory.setUsername(msg.getUsername());
        danmuHistory.setDisplayId(displayId);
        danmuHistory.setBadgeName(msg.getBadgeName());
        danmuHistory.setBadgeLevel((int) msg.getBadgeLevel()); // 显式转换为int
        danmuHistory.setContent(content);
        //  liveDanmuHistoryRepository.save(danmuHistory);

        // TODO 可以用大模型进行FAQ回复
//         String answer = content + "  的回复";
//         DouyinLiveChatClient.sendDanmu(answer);
        DouyinLiveChatClient douyinLiveChatClient = getDouyinLiveChatClient();
        if (douyinLiveChatClient != null) {
            DouyinRoomInitResult roomInitResult = douyinLiveChatClient.getRoomInitResult();
            if (roomInitResult != null) {
                RoomLiveStatusEnum roomLiveStatus = roomInitResult.getRoomLiveStatus();
                log.info("直播间状态 {}", roomLiveStatus);
            }
        }
    }

    /**
     * 处理接收到的礼物消息
     *
     * @param binaryFrameHandler 二进制帧处理器
     * @param msg                礼物消息体
     */
    @Override
    public void onGiftMsg(DouyinBinaryFrameHandler binaryFrameHandler, DouyinGiftMsg msg) {
        IDouyinMsgListener.super.onGiftMsg(binaryFrameHandler, msg);
        Object roomId = binaryFrameHandler.getRoomId();
        String uid = msg.getUid();
        String username = msg.getUsername();
        String giftName = msg.getGiftName();
        String giftId = String.valueOf(msg.getGiftId());
        Integer giftCount = msg.getGiftCount();
        Integer giftPrice = msg.getGiftPrice(); // 假设有getGiftPrice方法

        log.info("{} 收到礼物 {} {}({}) {} {}({})x{}({})",
                roomId, msg.getBadgeLevel() != 0 ? msg.getBadgeLevel() + msg.getBadgeName() : "",
                username, uid,
                giftName, giftId, giftCount, giftPrice);

        // 保存礼物消息到数据库
        LiveGiftHistory giftHistory = new LiveGiftHistory();
        giftHistory.setPlatform("douyin");
        giftHistory.setRoomId(String.valueOf(roomId));
        giftHistory.setUid(uid);
        giftHistory.setUsername(username);
        giftHistory.setGiftId(giftId);
        giftHistory.setGiftName(giftName);
        giftHistory.setGiftCount(giftCount);
        giftHistory.setGiftPrice(giftPrice);
        //   liveGiftHistoryRepository.save(giftHistory);
    }

    /**
     * 处理接收到的用户入场消息
     *
     * @param binaryFrameHandler 二进制帧处理器
     * @param msg                入场消息体
     */
    @Override
    public void onEnterRoomMsg(DouyinBinaryFrameHandler binaryFrameHandler, DouyinEnterRoomMsg msg) {
        IDouyinMsgListener.super.onEnterRoomMsg(binaryFrameHandler, msg);

        Object roomId = binaryFrameHandler.getRoomId();
        tech.ordinaryroad.live.chat.client.codec.douyin.protobuf.User user = msg.getMsg().getUser();
        
        // 性别判断：只保留男性数据 (gender = 1)
        // gender: 0=未知, 1=男性, 2=女性
        int gender = user.getGender();
        if (gender != 1) {
            log.debug("{} 过滤非男性用户：{} (性别: {})", roomId, user.getNickName(), gender);
            return;
        }

        String uid = String.valueOf(user.getId());
        String username = user.getNickName();
        String displayId = user.getDisplayId();
        log.info("{} 收到入房消息 用户：{} 抖音号：{}", roomId, username, displayId);

        // 保存用户入场消息到数据库
        LiveEntryHistory entryHistory = new LiveEntryHistory();
        entryHistory.setPlatform("douyin");
        entryHistory.setRoomId(String.valueOf(roomId));
        entryHistory.setUid(uid);
        entryHistory.setUsername(username);
        entryHistory.setDisplayId(displayId);
        liveEntryHistoryRepository.save(entryHistory);
    }

    /**
     * 处理接收到的房间统计消息 (例如点赞数、观看人数等)
     *
     * @param binaryFrameHandler 二进制帧处理器
     * @param msg                房间统计消息体
     */
    @Override
    public void onRoomStatsMsg(DouyinBinaryFrameHandler binaryFrameHandler, DouyinRoomStatsMsg msg) {
        IDouyinMsgListener.super.onRoomStatsMsg(binaryFrameHandler, msg);
        Object roomId = binaryFrameHandler.getRoomId();
        log.debug("{} 统计信息 累计点赞数: {}, 当前观看人数: {}, 累计观看人数: {}", roomId, msg.getLikedCount(), msg.getWatchingCount(), msg.getWatchedCount());

        // 保存房间统计消息到数据库
        LiveRoomStatsHistory statsHistory = new LiveRoomStatsHistory();
        statsHistory.setPlatform("douyin");
        statsHistory.setRoomId(String.valueOf(roomId));
        String likedCount = msg.getLikedCount();
        if (StringUtils.isNotBlank(likedCount)) {
            statsHistory.setLikedCount(Long.valueOf(likedCount));
        }
        if (StringUtils.isNotBlank(msg.getWatchingCount())) {
            statsHistory.setWatchingCount(Integer.valueOf(msg.getWatchingCount()));
        }
        if (StringUtils.isNotBlank(msg.getWatchedCount())) {
            statsHistory.setWatchedCount(Long.valueOf(msg.getWatchedCount()));
        }
        //   liveRoomStatsHistoryRepository.save(statsHistory);
    }

    /**
     * 处理接收到的通用CMD消息
     *
     * @param binaryFrameHandler 二进制帧处理器
     * @param cmd                CMD枚举类型
     * @param cmdMsg             CMD消息体
     */
    @Override
    public void onCmdMsg(DouyinBinaryFrameHandler binaryFrameHandler, DouyinCmdEnum cmd, ICmdMsg<DouyinCmdEnum> cmdMsg) {
        IDouyinMsgListener.super.onCmdMsg(binaryFrameHandler, cmd, cmdMsg);
        if (cmd == null && cmdMsg instanceof DouyinCmdMsg) {
            DouyinCmdMsg douyinCmdMsg = (DouyinCmdMsg) cmdMsg;
            String method = douyinCmdMsg.getMsg().getMethod();
            // 在这里拦截并处理特定的未知 CMD
            switch (method) {
                case "WebcastRoomRankMessage":
                    log.debug("{} 收到排行榜消息", binaryFrameHandler.getRoomId());
                    break;
                case "WebcastRoomDataSyncMessage":
                    log.debug("{} 收到房间数据同步消息", binaryFrameHandler.getRoomId());
                    break;
                default:
                    // 其他暂不处理的消息可以保持默认或打印简要日志
                    break;
            }
        }
    }

    /**
     * 处理所有类型的原始消息
     *
     * @param msg 原始消息对象
     */
    @Override
    public void onMsg(IMsg msg) {
    }

    /**
     * 处理接收到的未知CMD消息
     *
     * @param cmdString 未知CMD的字符串表示
     * @param msg       原始消息对象
     */
    @Override
    public void onUnknownCmd(String cmdString, IMsg msg) {
        // 已经在 onCmdMsg 中通过 method 进行了细分处理，这里可以按需保留或关闭
        log.debug("收到未知CMD消息 {}", cmdString);
    }
}