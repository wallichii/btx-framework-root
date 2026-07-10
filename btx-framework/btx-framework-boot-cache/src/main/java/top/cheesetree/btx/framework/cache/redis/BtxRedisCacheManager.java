package top.cheesetree.btx.framework.cache.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import top.cheesetree.btx.framework.cache.BtxCacheManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: van
 * @Date: 2020/9/27 13:37
 * @Description: TODO
 */
@Slf4j
public class BtxRedisCacheManager extends RedisCacheManager implements BtxCacheManager {
    private final RedisCacheWriter cacheWriter;

    @Autowired
    BtxRedisCacheProperties btxRedisCacheProperties;

    public static final Map<String, BtxRedisConfigProperties> CONFIG_MAP = new ConcurrentHashMap<>();

    public BtxRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration,
                                Map<String, RedisCacheConfiguration> initialCacheConfigurations) {
        super(cacheWriter, defaultCacheConfiguration, initialCacheConfigurations);
        this.cacheWriter = cacheWriter;
    }


    public static RedisCacheConfiguration createRedisCacheConfiguration(BtxRedisConfigProperties defaultCacheConfig) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        if (defaultCacheConfig.getTimeToLive() != null) {
            redisCacheConfiguration = redisCacheConfiguration.entryTtl(defaultCacheConfig.getTimeToLive());
        }

        if (Boolean.TRUE.equals(defaultCacheConfig.getUseKeyPrefix()) && defaultCacheConfig.getKeyPrefix() != null && !defaultCacheConfig.getKeyPrefix().isEmpty()) {
            redisCacheConfiguration =
                    redisCacheConfiguration.computePrefixWith(cacheName -> defaultCacheConfig.getKeyPrefix() + cacheName + ":");
        }

        if (Boolean.FALSE.equals(defaultCacheConfig.getCacheNullValues())) {
            redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
        }


        redisCacheConfiguration =
                redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new BtxFastJsonRedisSerializer()));
        return redisCacheConfiguration;
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        BtxRedisCacheProperties defaultBtxCacheConfig = btxRedisCacheProperties;
        BtxRedisConfigProperties btxCacheConfig = btxRedisCacheProperties.getCaches().get(name);

        if (btxCacheConfig == null) {
            btxCacheConfig = defaultBtxCacheConfig;
            if (CONFIG_MAP.containsKey(name)) {
                btxCacheConfig.setDefaultValues(CONFIG_MAP.get(name));
            }
        } else {
            btxCacheConfig.setDefaultValues(defaultBtxCacheConfig);
        }

        cacheConfig = createRedisCacheConfiguration(btxCacheConfig);
        BtxRedisCache redisCache = new BtxRedisCache(name, cacheWriter, cacheConfig);

        return redisCache;
    }
}
