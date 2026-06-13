package tech.ordinaryroad.live.chat.client.example.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.ordinaryroad.live.chat.client.example.client.entity.LiveEntryHistory;
import tech.ordinaryroad.live.chat.client.example.client.model.EntryExportRequest;
import tech.ordinaryroad.live.chat.client.example.client.model.EntryQueryRequest;
import tech.ordinaryroad.live.chat.client.example.client.service.LiveEntryHistoryService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播间用户入场记录管理控制器
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Slf4j
@RestController
@RequestMapping("/api/live-entry")
public class LiveEntryHistoryController {

    @Autowired
    private LiveEntryHistoryService entryHistoryService;

    /**
     * 分页查询入场记录
     *
     * @param request 查询请求参数
     * @return 分页结果
     */
    @PostMapping("/query")
    public Map<String, Object> queryEntries(@RequestBody EntryQueryRequest request) {
        log.info("查询入场记录: roomId={}, platform={}", 
                request.getRoomId(), request.getPlatform());

        LocalDateTime start = request.getStartTime() != null ? LocalDateTime.parse(request.getStartTime()) : null;
        LocalDateTime end = request.getEndTime() != null ? LocalDateTime.parse(request.getEndTime()) : null;

        Page<LiveEntryHistory> page = entryHistoryService.queryEntries(
                request.getRoomId(), 
                request.getPlatform(), 
                null,  // username 已废弃，传 null
                start, 
                end, 
                request.getPageNum() != null ? request.getPageNum() : 1,
                request.getPageSize() != null ? request.getPageSize() : 20
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", page.getContent());
        result.put("total", page.getTotalElements());
        result.put("pageNum", request.getPageNum() != null ? request.getPageNum() : 1);
        result.put("pageSize", request.getPageSize() != null ? request.getPageSize() : 20);
        result.put("totalPages", page.getTotalPages());

        return result;
    }

    /**
     * 获取单条记录详情
     *
     * @param id 记录ID
     * @return 记录详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getEntryDetail(@PathVariable Long id) {
        log.info("查询入场记录详情: id={}", id);

        LiveEntryHistory entry = entryHistoryService.getEntryById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", entry);

        return result;
    }

    /**
     * 删除单条记录
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteEntry(@PathVariable Long id) {
        log.info("删除入场记录: id={}", id);

        entryHistoryService.deleteEntry(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");

        return result;
    }

    /**
     * 批量删除记录
     *
     * @param ids ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch-delete")
    public Map<String, Object> batchDeleteEntries(@RequestBody List<Long> ids) {
        log.info("批量删除入场记录: count={}", ids.size());

        entryHistoryService.deleteEntries(ids);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量删除成功，共删除" + ids.size() + "条记录");

        return result;
    }

    /**
     * 删除指定直播间的所有记录
     *
     * @param roomId 直播间ID
     * @return 操作结果
     */
    @DeleteMapping("/room/{roomId}")
    public Map<String, Object> deleteByRoomId(@PathVariable String roomId) {
        log.info("删除直播间所有入场记录: roomId={}", roomId);

        entryHistoryService.deleteByRoomId(roomId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已删除直播间 " + roomId + " 的所有入场记录");

        return result;
    }

    /**
     * 清理过期数据
     *
     * @param days 保留天数
     * @return 操作结果
     */
    @DeleteMapping("/clean-old-data")
    public Map<String, Object> cleanOldData(@RequestParam(defaultValue = "30") int days) {
        log.info("清理{}天前的入场记录", days);

        long deletedCount = entryHistoryService.cleanOldData(days);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "清理完成，共删除" + deletedCount + "条记录");
        result.put("deletedCount", deletedCount);

        return result;
    }

    /**
     * 获取统计数据
     *
     * @param roomId 直播间ID（可选）
     * @param platform 平台标识（可选）
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String platform
    ) {
        log.info("获取入场记录统计: roomId={}, platform={}", roomId, platform);

        Map<String, Object> stats = entryHistoryService.getStatistics(roomId, platform);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);

        return result;
    }

    /**
     * 导出CSV文件
     *
     * @param request 导出请求参数
     * @return CSV文件
     */
    @PostMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@RequestBody EntryExportRequest request) {
        log.info("导出CSV: roomId={}, platform={}", 
                request.getRoomId(), request.getPlatform());

        LocalDateTime start = request.getStartTime() != null ? LocalDateTime.parse(request.getStartTime()) : null;
        LocalDateTime end = request.getEndTime() != null ? LocalDateTime.parse(request.getEndTime()) : null;

        String csvContent = entryHistoryService.exportToCsv(
                request.getRoomId(), 
                request.getPlatform(), 
                null,  // username 已废弃，传 null
                start, 
                end
        );

        // 生成文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "live_entry_" + timestamp + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", 
            new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标记记录为已使用
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/{id}/mark-used")
    public Map<String, Object> markAsUsed(@PathVariable Long id) {
        log.info("标记入场记录为已使用: id={}", id);

        entryHistoryService.markAsUsed(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "标记成功");

        return result;
    }

    /**
     * 批量标记记录为已使用
     *
     * @param ids ID列表
     * @return 操作结果
     */
    @PostMapping("/batch-mark-used")
    public Map<String, Object> batchMarkAsUsed(@RequestBody List<Long> ids) {
        log.info("批量标记入场记录为已使用: count={}", ids.size());

        entryHistoryService.markMultipleAsUsed(ids);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "批量标记成功，共标记" + ids.size() + "条记录");

        return result;
    }

    /**
     * 查询未使用的入场记录
     *
     * @param roomId 直播间ID
     * @param limit 限制数量（默认100）
     * @return 未使用的记录列表
     */
    @GetMapping("/unused")
    public Map<String, Object> getUnusedEntries(
            @RequestParam String roomId,
            @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("查询未使用的入场记录: roomId={}, limit={}", roomId, limit);

        List<LiveEntryHistory> entries = entryHistoryService.findUnusedEntries(roomId, limit);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", entries);
        result.put("count", entries.size());

        return result;
    }

    /**
     * 取数功能：获取一个未使用的抖音号并标记为已使用
     *
     * @param platform 平台标识（如douyin）
     * @return 返回一条未使用的入场记录，如果不存在则返回null
     */
    @PostMapping("/fetch-display-id")
    public Map<String, Object> fetchDisplayId(@RequestParam String platform) {
        log.info("取数请求: platform={}", platform);

        LiveEntryHistory entry = entryHistoryService.fetchAndMarkDisplayId(platform);

        Map<String, Object> result = new HashMap<>();
        if (entry != null) {
            result.put("success", true);
            result.put("message", "成功获取抖音号");
            result.put("data", entry);
            result.put("displayId", entry.getDisplayId());
            result.put("uid", entry.getUid());
            result.put("username", entry.getUsername());
        } else {
            result.put("success", false);
            result.put("message", "没有可用的未使用记录");
        }

        return result;
    }

    /**
     * 根据抖音号查询未使用的记录
     *
     * @param displayId 抖音号
     * @return 未使用的记录列表
     */
    @GetMapping("/search-by-display-id")
    public Map<String, Object> searchByDisplayId(@RequestParam String displayId) {
        log.info("根据抖音号查询未使用记录: displayId={}", displayId);

        List<LiveEntryHistory> entries = entryHistoryService.findUnusedByDisplayId(displayId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", entries);
        result.put("count", entries.size());

        return result;
    }
}
