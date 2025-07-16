package top.cheesetree.btx.framework.webflux.security.core.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxCacheProperties;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityAuthUserDTO;

import java.util.concurrent.TimeUnit;

/**
 * @author van
 * @date 2022/3/21 15:52
 * @description TODO
 */
@ConditionalOnExpression("${btx.security.webflux.cache.enabled:false} && '${btx.security.webflux.cache" +
        ".cache-type:CAFFEINE}'.equalsIgnoreCase('CAFFEINE')")
@Configuration
@Slf4j
public class BtxWebfluxCaffeineConfigure {
    @Autowired
    BtxWebfluxCacheProperties btxWebfluxCacheProperties;

    @Autowired
    @Lazy
    Cache<String, WebfluxSecurityAuthUserDTO> caffeineCache;

    @Bean
    public Cache<String, WebfluxSecurityAuthUserDTO> caffeineCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(btxWebfluxCacheProperties.getCacheExpire(), TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(256)
                .recordStats()
                .removalListener((key, value, cause) -> log.info(">>> 删除缓存 [{}]({}), reason is [{}]", key, value,
                        cause))
                .build();
    }

    @Bean("btxWebfluxCaffeineCache")
    public BtxWebfluxCaffeineCache<String, WebfluxSecurityAuthUserDTO> btxWebfluxCaffeineCache() {
        return new BtxWebfluxCaffeineCache(caffeineCache);
    }


}
