package top.cheesetree.btx.framework.cache.redis;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: van
 * @Date: 2020/9/27 14:07
 * @Description: TODO
 */

@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
@EnableCaching(proxyTargetClass = true)
@Configuration
@EnableConfigurationProperties({BtxRedisCacheProperties.class})
@Slf4j
public class BtxRedisCacheConfigure implements CachingConfigurer {
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private BtxRedisConfigProperties btxRedisConfigProperties;
    @Autowired
    private BtxRedisCacheProperties btxRedisCacheProperties;

    @Override
    @Bean
    public CacheManager cacheManager() {
        BtxRedisConfigProperties defaultBtxCacheConfig = btxRedisConfigProperties;

        RedisCacheConfiguration defaultCacheConfig =
                BtxRedisCacheManager.createRedisCacheConfiguration(defaultBtxCacheConfig);

        defaultCacheConfig =
                defaultCacheConfig.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new BtxFastJsonRedisSerializer<>()));

        Map<String, RedisCacheConfiguration> initialCacheConfiguration =
                new HashMap<>(btxRedisCacheProperties.getCaches().size());
        for (String cacheName : btxRedisCacheProperties.getCaches().keySet()) {
            BtxRedisConfigProperties btxCacheConfig = btxRedisCacheProperties.getCaches().get(cacheName);
            btxCacheConfig.setDefaultValues(defaultBtxCacheConfig);
            RedisCacheConfiguration cacheConfig =
                    BtxRedisCacheManager.createRedisCacheConfiguration(btxCacheConfig);
            cacheConfig = cacheConfig.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new BtxFastJsonRedisSerializer<>()));
            initialCacheConfiguration.put(cacheName, cacheConfig);
        }

        JSONFactory.getDefaultObjectWriterProvider().setNamingStrategy(PropertyNamingStrategy.CamelCase1x);

        return new BtxRedisCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
                defaultCacheConfig, initialCacheConfiguration);
    }

    @Bean(name = "redisTemplateFactory")
    public RedisTemplateFactoryImpl redisTemplateFactory() {
        return new RedisTemplateFactoryImpl();
    }

}
