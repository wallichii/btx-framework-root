package top.cheesetree.btx.framework.webflux.security.cache.redis;


import top.cheesetree.btx.framework.cache.redis.RedisTemplateFactoryImpl;
import top.cheesetree.btx.framework.webflux.security.core.cache.IBtxWebfluxCache;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxCacheProperties;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;

import java.util.concurrent.TimeUnit;

/**
 * @author van
 * @date 2022/2/15 17:22
 * @description TODO
 */
public class BtxWebfluxRedisCache implements IBtxWebfluxCache<String, AuthenticationInfo> {
    private RedisTemplateFactoryImpl redisTemplateFactory;
    private BtxWebfluxCacheProperties btxWebfluxCacheProperties;

    public BtxWebfluxRedisCache(RedisTemplateFactoryImpl redisTemplateFactory,
                                BtxWebfluxCacheProperties btxWebfluxCacheProperties) {
        this.redisTemplateFactory = redisTemplateFactory;
        this.btxWebfluxCacheProperties = btxWebfluxCacheProperties;
    }

    @Override
    public AuthenticationInfo get(String k) {
        return redisTemplateFactory.generateRedisTemplate(AuthenticationInfo.class).opsForValue().get(k);
    }

    @Override
    public void add(String k, AuthenticationInfo v, long expire) {
        redisTemplateFactory.generateRedisTemplate(AuthenticationInfo.class).opsForValue().set(k, v, expire, TimeUnit.SECONDS);
    }

    @Override
    public boolean containsKey(String k) {
        return redisTemplateFactory.generateRedisTemplate(AuthenticationInfo.class).hasKey(k);
    }

    @Override
    public void del(String k) {
        redisTemplateFactory.generateRedisTemplate(AuthenticationInfo.class).delete(k);
    }

    @Override
    public IBtxWebfluxCache<String, AuthenticationInfo> getInstance() {
        return new BtxWebfluxRedisCache(this.redisTemplateFactory, this.btxWebfluxCacheProperties);
    }

}
