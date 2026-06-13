package tech.ordinaryroad.live.chat.client.example.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.ordinaryroad.live.chat.client.example.client.entity.PlatformConfig;

import java.util.Optional;

/**
 * 平台配置数据访问层
 *
 * @author OrdinaryRoad
 * @date 2026/04/04
 */
@Repository
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, Long> {

    /**
     * 根据平台标识查询配置
     *
     * @param platform 平台标识
     * @return 平台配置
     */
    Optional<PlatformConfig> findByPlatform(String platform);

    /**
     * 检查平台配置是否存在
     *
     * @param platform 平台标识
     * @return 是否存在
     */
    boolean existsByPlatform(String platform);

    /**
     * 删除平台配置
     *
     * @param platform 平台标识
     */
    void deleteByPlatform(String platform);
}
