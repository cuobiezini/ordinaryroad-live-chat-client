package tech.ordinaryroad.live.chat.client.example.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.ordinaryroad.live.chat.client.example.client.entity.ApiKeyConfig;

import java.util.Optional;

/**
 * API Key 配置 Repository
 *
 * @author OrdinaryRoad
 * @date 2026/06/13
 */
@Repository
public interface ApiKeyConfigRepository extends JpaRepository<ApiKeyConfig, Long> {

    /**
     * 根据 API Key 查询配置
     *
     * @param apiKey API Key
     * @return API Key 配置
     */
    Optional<ApiKeyConfig> findByApiKey(String apiKey);

    /**
     * 检查 API Key 是否存在且启用
     *
     * @param apiKey API Key
     * @return 是否存在且启用
     */
    boolean existsByApiKeyAndEnabledTrue(String apiKey);
}
