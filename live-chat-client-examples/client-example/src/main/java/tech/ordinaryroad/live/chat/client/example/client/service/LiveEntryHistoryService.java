package tech.ordinaryroad.live.chat.client.example.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveEntryHistory;
import tech.ordinaryroad.live.chat.client.example.client.repository.LiveEntryHistoryRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播间用户入场记录服务
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@Service
public class LiveEntryHistoryService {

    @Autowired
    private LiveEntryHistoryRepository entryHistoryRepository;

    /**
     * 分页查询入场记录（支持多条件）
     *
     * @param roomId 直播间ID
     * @param platform 平台标识
     * @param username 用户昵称（模糊查询）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public Page<LiveEntryHistory> queryEntries(
            String roomId,
            String platform,
            String username,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNum,
            int pageSize
    ) {
        // 参数验证
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        log.info("查询入场记录: roomId={}, platform={}, username={}, timeRange=[{} - {}], page={}/{}",
                roomId, platform, username, startTime, endTime, pageNum, pageSize);

        return entryHistoryRepository.findByConditions(
                roomId != null && !roomId.isEmpty() ? roomId : null,
                platform != null && !platform.isEmpty() ? platform : null,
                username != null && !username.isEmpty() ? username : null,
                startTime,
                endTime,
                pageable
        );
    }

    /**
     * 根据ID查询单条记录
     *
     * @param id 记录ID
     * @return 入场记录
     */
    public LiveEntryHistory getEntryById(Long id) {
        return entryHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("入场记录不存在: " + id));
    }

    /**
     * 删除单条记录
     *
     * @param id 记录ID
     */
    @Transactional
    public void deleteEntry(Long id) {
        log.info("删除入场记录: id={}", id);
        entryHistoryRepository.deleteById(id);
    }

    /**
     * 批量删除记录
     *
     * @param ids ID列表
     */
    @Transactional
    public void deleteEntries(Iterable<Long> ids) {
        log.info("批量删除入场记录: count={}", ((java.util.Collection<?>) ids).size());
        entryHistoryRepository.deleteAllById(ids);
    }

    /**
     * 删除指定直播间的所有记录
     *
     * @param roomId 直播间ID
     */
    @Transactional
    public void deleteByRoomId(String roomId) {
        log.info("删除直播间所有入场记录: roomId={}", roomId);
        entryHistoryRepository.deleteByRoomId(roomId);
    }

    /**
     * 清理所有入场记录
     *
     * @return 删除的记录数
     */
    @Transactional
    public long cleanAllData() {
        log.info("开始清理所有入场记录");
        
        // 获取总记录数
        long totalCount = entryHistoryRepository.count();
        
        if (totalCount == 0) {
            log.info("没有需要清理的数据");
            return 0;
        }
        
        // 使用JPQL批量删除，避免乐观锁冲突
        int deletedCount = entryHistoryRepository.deleteAllRecords();
        
        log.info("清理完成: 删除{}条记录", deletedCount);
        return deletedCount;
    }

    /**
     * 获取统计数据
     *
     * @param roomId 直播间ID（可选）
     * @param platform 平台标识（可选）
     * @return 统计信息
     */
    public Map<String, Object> getStatistics(String roomId, String platform) {
        Map<String, Object> stats = new HashMap<>();
        
        if (roomId != null && !roomId.isEmpty()) {
            // 直播间级别的统计
            long totalEntries = entryHistoryRepository.countByRoomId(roomId);
            long uniqueVisitors = entryHistoryRepository.countDistinctUidByRoomId(roomId);
            
            stats.put("roomId", roomId);
            stats.put("totalEntries", totalEntries);
            stats.put("uniqueVisitors", uniqueVisitors);
        } else if (platform != null && !platform.isEmpty()) {
            // 平台级别的统计
            long totalEntries = entryHistoryRepository.countByPlatform(platform);
            
            stats.put("platform", platform);
            stats.put("totalEntries", totalEntries);
        } else {
            // 全局统计
            long totalEntries = entryHistoryRepository.count();
            
            stats.put("totalEntries", totalEntries);
        }
        
        return stats;
    }

    /**
     * 导出CSV数据（简化版）
     *
     * @param roomId 直播间ID
     * @param platform 平台标识
     * @param username 用户昵称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return CSV格式字符串
     */
    public String exportToCsv(
            String roomId,
            String platform,
            String username,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,平台,直播间ID,用户UID,用户昵称,抖音号,是否已使用,进入时间\n");
        
        // 查询所有符合条件的数据（不分页）
        Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LiveEntryHistory> page = entryHistoryRepository.findByConditions(
                roomId != null && !roomId.isEmpty() ? roomId : null,
                platform != null && !platform.isEmpty() ? platform : null,
                username != null && !username.isEmpty() ? username : null,
                startTime,
                endTime,
                allPageable
        );
        
        for (LiveEntryHistory entry : page.getContent()) {
            csv.append(entry.getId()).append(",")
               .append(entry.getPlatform()).append(",")
               .append(entry.getRoomId()).append(",")
               .append(entry.getUid()).append(",")
               .append(escapeCsv(entry.getUsername())).append(",")
               .append(escapeCsv(entry.getDisplayId())).append(",")
               .append(Boolean.TRUE.equals(entry.getIsUsed()) ? "是" : "否").append(",")
               .append(entry.getCreatedAt()).append("\n");
        }
        
        return csv.toString();
    }

    /**
     * 标记记录为已使用
     *
     * @param id 记录ID
     */
    @Transactional
    public void markAsUsed(Long id) {
        log.info("标记入场记录为已使用: id={}", id);
        LiveEntryHistory entry = getEntryById(id);
        entry.setIsUsed(true);
        entryHistoryRepository.save(entry);
    }

    /**
     * 批量标记记录为已使用
     *
     * @param ids ID列表
     */
    @Transactional
    public void markMultipleAsUsed(Iterable<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            try {
                LiveEntryHistory entry = getEntryById(id);
                entry.setIsUsed(true);
                entryHistoryRepository.save(entry);
                count++;
            } catch (Exception e) {
                log.error("标记记录为已使用失败: id={}, error={}", id, e.getMessage());
            }
        }
        log.info("批量标记完成: 成功{}条", count);
    }

    /**
     * 根据条件查询未使用的记录
     *
     * @param roomId 直播间ID
     * @param limit 限制数量
     * @return 未使用的记录列表
     */
    public List<LiveEntryHistory> findUnusedEntries(String roomId, int limit) {
        if (limit < 1 || limit > 1000) {
            limit = 100;
        }
        
        log.info("查询未使用的入场记录: roomId={}, limit={}", roomId, limit);
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt"));
        return entryHistoryRepository.findByRoomIdAndIsUsedFalse(roomId, pageable).getContent();
    }

    /**
     * 取数功能：获取一个未使用的抖音号并标记为已使用
     *
     * @param platform 平台标识（如douyin）
     * @return 返回一条未使用的入场记录，如果不存在则返回null
     */
    @Transactional
    public LiveEntryHistory fetchAndMarkDisplayId(String platform) {
        log.info("取数功能: platform={}", platform);
        
        // 查询第一条未使用的记录
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<LiveEntryHistory> page = entryHistoryRepository.findByPlatformAndIsUsedFalse(platform, pageable);
        
        if (page.hasContent()) {
            LiveEntryHistory entry = page.getContent().get(0);
            // 标记为已使用
            entry.setIsUsed(true);
            entryHistoryRepository.save(entry);
            log.info("成功取数: id={}, displayId={}, uid={}", entry.getId(), entry.getDisplayId(), entry.getUid());
            return entry;
        } else {
            log.warn("没有可用的未使用记录: platform={}", platform);
            return null;
        }
    }

    /**
     * 根据抖音号查询未使用的记录
     *
     * @param displayId 抖音号
     * @return 未使用的记录列表
     */
    public List<LiveEntryHistory> findUnusedByDisplayId(String displayId) {
        log.info("根据抖音号查询未使用记录: displayId={}", displayId);
        return entryHistoryRepository.findByDisplayIdAndIsUsedFalse(displayId);
    }

    /**
     * CSV字段转义
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
