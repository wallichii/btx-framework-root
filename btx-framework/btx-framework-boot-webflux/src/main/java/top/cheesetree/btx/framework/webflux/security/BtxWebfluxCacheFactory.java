package top.cheesetree.btx.framework.webflux.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import top.cheesetree.btx.framework.webflux.security.core.cache.IBtxWebfluxCache;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityAuthUserDTO;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author van
 * @date 2022/6/16 13:13
 * @description TODO
 */
@Component
@ConditionalOnProperty(name = "btx.webflux.security.cache.enabled", havingValue = "true")
public class BtxWebfluxCacheFactory {
    private static Map<KeyValueMapKey, IBtxWebfluxCache<?, ?>> redisTemplateMap = new ConcurrentHashMap<>();

    @Autowired
    IBtxWebfluxCache btxWebfluxCache;

    public <TKey, TValue> IBtxWebfluxCache<TKey, TValue> generateCache(Class<TKey> keyClz,
                                                                                          Class<TValue> valueClz) {
        KeyValueMapKey redisTemplateMapKey = new KeyValueMapKey(keyClz, valueClz);
        IBtxWebfluxCache<TKey, TValue> result =
                (IBtxWebfluxCache<TKey, TValue>) redisTemplateMap.get(redisTemplateMapKey);
        if (result == null) {
            result = btxWebfluxCache.getInstance();
            redisTemplateMap.put(redisTemplateMapKey, result);
        }

        return result;
    }

    public IBtxWebfluxCache<String, WebfluxSecurityAuthUserDTO> generateCache() {
        return generateCache(String.class, WebfluxSecurityAuthUserDTO.class);
    }

    @Getter
    @Setter
    public static class KeyValueMapKey implements Serializable {
        private Class<?> keyClass;
        private Class<?> valueClass;

        public KeyValueMapKey(Class<?> keyClass, Class<?> valueClass) {
            super();
            this.keyClass = keyClass;
            this.valueClass = valueClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            KeyValueMapKey that = (KeyValueMapKey) o;
            return keyClass.equals(that.keyClass) && valueClass.equals(that.valueClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyClass, valueClass);
        }
    }
}
