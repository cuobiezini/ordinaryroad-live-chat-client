package tech.ordinaryroad.live.chat.client.example.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveEntryHistory;

/**
 * 直播间用户入场记录数据访问层
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Repository
public interface LiveEntryHistoryRepository extends JpaRepository<LiveEntryHistory, Long> {
}
