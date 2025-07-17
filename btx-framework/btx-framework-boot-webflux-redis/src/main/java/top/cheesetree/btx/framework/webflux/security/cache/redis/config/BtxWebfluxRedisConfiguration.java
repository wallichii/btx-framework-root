package top.cheesetree.btx.framework.webflux.security.cache.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import top.cheesetree.btx.framework.cache.redis.RedisTemplateFactoryImpl;
import top.cheesetree.btx.framework.webflux.security.cache.redis.BtxWebfluxRedisCache;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxCacheProperties;

/**
 * @author van
 * @date 2025/7/16 17:31
 * @description TODO
 */
@ConditionalOnExpression("${btx.security.webflux.cache.enabled:false} && '${btx.security.webflux.cache" +
        ".cache-type:REDIS}'.equalsIgnoreCase('REDIS')")
@Configuration
@Slf4j
public class BtxWebfluxRedisConfiguration {
    @Autowired
    @Lazy
    BtxWebfluxCacheProperties btxWebfluxCacheProperties;
    @Autowired
    @Lazy
    RedisTemplateFactoryImpl redisTemplateFactory;


    @Bean("btxWebfluxRedisCache")
    public BtxWebfluxRedisCache btxWebfluxRedisCache() {
        return new BtxWebfluxRedisCache(redisTemplateFactory, btxWebfluxCacheProperties);
    }

}
