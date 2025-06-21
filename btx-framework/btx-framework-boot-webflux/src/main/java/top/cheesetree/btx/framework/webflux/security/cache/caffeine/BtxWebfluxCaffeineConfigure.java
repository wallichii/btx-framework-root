package top.cheesetree.btx.framework.webflux.security.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.smartplg.gourd.project.security.springsecurity.config.GourdSpringSecurityCacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityAuthUserDTO;

import java.util.concurrent.TimeUnit;

/**
 * @author van
 * @date 2022/3/21 15:52
 * @description TODO
 */
@ConditionalOnExpression("${btx.weblux.security.cache.enabled:false} && '${btx.weblux.security.cache.cache-type:CAFFEINE}'.equalsIgnoreCase('CAFFEINE')")
@Configuration
@Slf4j
public class BtxWebfluxCaffeineConfigure {
    @Autowired
    GourdSpringSecurityCacheProperties gourdSpringSecurityCacheProperties;

    @Autowired
    @Lazy
    Cache caffeineCache;

    @Bean
    public Cache<String, WebfluxSecurityAuthUserDTO> caffeineCache() {
        return Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(gourdSpringSecurityCacheProperties.getCacheExpire(), TimeUnit.SECONDS)
                // 初始的缓存空间大小
                .initialCapacity(256)
                .recordStats()
                .removalListener((key, value, cause) -> log.info(">>> 删除缓存 [{}]({}), reason is [{}]", key, value, cause))
                .build();
    }

    @Bean("gourdSpringSecurityCache")
    public BtxWebfluxCaffeineCache gourdSpringSecurityCaffeineCache() {
        return new BtxWebfluxCaffeineCache(caffeineCache);
    }


}
