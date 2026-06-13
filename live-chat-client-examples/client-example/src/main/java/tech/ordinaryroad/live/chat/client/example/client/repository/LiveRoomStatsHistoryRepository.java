package tech.ordinaryroad.live.chat.client.example.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveRoomStatsHistory;

/**
 * 直播间统计数据历史数据访问层
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Repository
public interface LiveRoomStatsHistoryRepository extends JpaRepository<LiveRoomStatsHistory, Long> {
}
