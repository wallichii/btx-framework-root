package top.cheesetree.btx.framework.cache.redis;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * @Author: van
 * @Date: 2020/9/27 13:57
 * @Description: TODO
 */
@Getter
@Setter
public class BtxRedisConfigProperties {
    private Duration timeToLive;

    /**
     * 缓存Key前缀
     */
    private String keyPrefix;

    /**
     * 是否使用KeyPrefix.
     */
    private Boolean useKeyPrefix = true;

    /**
     * 缓存NULL值
     */
    private Boolean cacheNullValues;

    /**
     * evict操作支持, Pattern批量删除
     */
    private Boolean evictAllowPattern;

    public void setDefaultValues(BtxRedisConfigProperties defaultValue) {

        if (this.getTimeToLive() == null) {
            this.setTimeToLive(defaultValue.getTimeToLive());
        }

        if (this.getKeyPrefix() == null) {
            this.setKeyPrefix(defaultValue.getKeyPrefix());
        }

        if (this.getCacheNullValues() == null) {
            this.setCacheNullValues(defaultValue.getCacheNullValues());
        }

        if (this.getUseKeyPrefix() == null) {
            this.setUseKeyPrefix(defaultValue.getUseKeyPrefix());
        }

        if (this.getEvictAllowPattern() == null) {
            this.setEvictAllowPattern(defaultValue.getEvictAllowPattern());
        }
    }

}
