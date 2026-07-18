package tech.ordinaryroad.live.chat.client.example.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformDisplayConfig;

import java.util.List;
import java.util.Optional;

/**
 * 平台展示配置Repository
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Repository
public interface PlatformDisplayConfigRepository extends JpaRepository<PlatformDisplayConfig, Long> {

    /**
     * 根据平台标识查询
     *
     * @param platform 平台标识
     * @return 平台展示配置
     */
    Optional<PlatformDisplayConfig> findByPlatform(String platform);

    /**
     * 查询所有启用的平台配置，按显示顺序排序
     *
     * @return 启用的平台配置列表
     */
    List<PlatformDisplayConfig> findByEnabledTrueOrderByDisplayOrderAsc();

    /**
     * 查询所有平台配置，按显示顺序排序
     *
     * @return 所有平台配置列表
     */
    List<PlatformDisplayConfig> findAllByOrderByDisplayOrderAsc();

    /**
     * 删除指定平台的配置
     *
     * @param platform 平台标识
     */
    void deleteByPlatform(String platform);
}
