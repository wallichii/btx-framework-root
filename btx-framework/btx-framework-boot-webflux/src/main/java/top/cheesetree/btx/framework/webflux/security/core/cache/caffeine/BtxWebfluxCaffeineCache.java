package top.cheesetree.btx.framework.webflux.security.core.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import top.cheesetree.btx.framework.webflux.security.core.cache.IBtxWebfluxCache;

/**
 * @author van
 * @date 2022/3/21 15:28
 * @description TODO
 */
public class BtxWebfluxCaffeineCache<K, V> implements IBtxWebfluxCache<K,
        V> {

    Cache<K, V> caffeineCache;

    @Override
    public void add(K key, V authentication, long expire) {
        caffeineCache.put(key, authentication);
    }

    @Override
    public boolean containsKey(K key) {
        return caffeineCache.asMap().containsKey(key);
    }

    @Override
    public V get(K key) {
        return caffeineCache.asMap().get(key);
    }

    @Override
    public void del(K key) {
        caffeineCache.asMap().remove(key);
    }

    @Override
    public IBtxWebfluxCache getInstance() {
        return new BtxWebfluxCaffeineCache(caffeineCache);
    }

    public BtxWebfluxCaffeineCache(Cache<K, V> caffeineCache) {
        this.caffeineCache = caffeineCache;
    }
}
