package tech.ordinaryroad.live.chat.client.example.client.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveEntryHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 直播间用户入场记录Repository
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Repository
public interface LiveEntryHistoryRepository extends JpaRepository<LiveEntryHistory, Long> {

    /**
     * 根据直播间ID查询（分页）
     *
     * @param roomId 直播间ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LiveEntryHistory> findByRoomIdOrderByCreatedAtDesc(String roomId, Pageable pageable);

    /**
     * 根据平台查询（分页）
     *
     * @param platform 平台标识
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LiveEntryHistory> findByPlatformOrderByCreatedAtDesc(String platform, Pageable pageable);

    /**
     * 根据用户UID查询（分页）
     *
     * @param uid 用户UID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LiveEntryHistory> findByUidOrderByCreatedAtDesc(String uid, Pageable pageable);

    /**
     * 多条件查询（分页）
     *
     * @param roomId 直播间ID
     * @param platform 平台标识
     * @param username 用户昵称（模糊查询）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT e FROM LiveEntryHistory e WHERE " +
           "(:roomId IS NULL OR e.roomId = :roomId) AND " +
           "(:platform IS NULL OR e.platform = :platform) AND " +
           "(:username IS NULL OR e.username LIKE %:username%) AND " +
           "(:startTime IS NULL OR e.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR e.createdAt <= :endTime) " +
           "ORDER BY e.createdAt DESC")
    Page<LiveEntryHistory> findByConditions(
            @Param("roomId") String roomId,
            @Param("platform") String platform,
            @Param("username") String username,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    /**
     * 统计指定直播间的入场人数
     *
     * @param roomId 直播间ID
     * @return 入场人数
     */
    long countByRoomId(String roomId);

    /**
     * 统计指定平台的入场人数
     *
     * @param platform 平台标识
     * @return 入场人数
     */
    long countByPlatform(String platform);

    /**
     * 统计指定时间段内的入场人数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 入场人数
     */
    @Query("SELECT COUNT(e) FROM LiveEntryHistory e WHERE e.createdAt >= :startTime AND e.createdAt <= :endTime")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定直播间的独立访客数（去重UID）
     *
     * @param roomId 直播间ID
     * @return 独立访客数
     */
    @Query("SELECT COUNT(DISTINCT e.uid) FROM LiveEntryHistory e WHERE e.roomId = :roomId")
    long countDistinctUidByRoomId(@Param("roomId") String roomId);

    /**
     * 删除指定直播间的所有记录
     *
     * @param roomId 直播间ID
     */
    void deleteByRoomId(String roomId);

    /**
     * 删除指定时间之前的记录
     *
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    long deleteByCreatedAtBefore(LocalDateTime beforeTime);

    /**
     * 查询未使用的入场记录（分页）
     *
     * @param roomId 直播间ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LiveEntryHistory> findByRoomIdAndIsUsedFalse(String roomId, Pageable pageable);

    /**
     * 统计未使用的记录数
     *
     * @param roomId 直播间ID
     * @return 未使用记录数
     */
    long countByRoomIdAndIsUsedFalse(String roomId);

    /**
     * 查询指定平台未使用的入场记录（分页）
     *
     * @param platform 平台标识
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<LiveEntryHistory> findByPlatformAndIsUsedFalse(String platform, Pageable pageable);

    /**
     * 根据抖音号查询未使用的记录
     *
     * @param displayId 抖音号
     * @return 未使用的记录列表
     */
    List<LiveEntryHistory> findByDisplayIdAndIsUsedFalse(String displayId);

    /**
     * 检查是否存在相同平台、直播间和displayId的记录
     *
     * @param platform  平台标识
     * @param roomId    直播间ID
     * @param displayId 展示ID（如快手号）
     * @return 是否存在
     */
    boolean existsByPlatformAndRoomIdAndDisplayId(String platform, String roomId, String displayId);

    /**
     * 批量删除所有记录（JPQL方式，避免乐观锁冲突）
     *
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM LiveEntryHistory")
    int deleteAllRecords();
}