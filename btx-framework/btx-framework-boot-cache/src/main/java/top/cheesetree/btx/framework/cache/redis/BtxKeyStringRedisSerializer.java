package top.cheesetree.btx.framework.cache.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import top.cheesetree.btx.framework.core.constants.BtxConsts;

import java.nio.charset.StandardCharsets;

/**
 * @Author: van
 * @Date: 2021/9/22 10:37
 * @Description: TODO
 */
public class BtxKeyStringRedisSerializer implements RedisSerializer<String> {
    private String keyprefix;
    private final int prefixLen;

    public BtxKeyStringRedisSerializer(String keyprefix) {
        this.keyprefix = keyprefix;
        this.prefixLen = keyprefix.length();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.serializer.RedisSerializer#deserialize(byte[])
     */
    @Override
    public String deserialize(@Nullable byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        String fullKey = new String(bytes, StandardCharsets.UTF_8);
        // 只判断【是否以指定前缀开头】，精准截取，不用正则
        if (fullKey.startsWith(keyprefix)) {
            return fullKey.substring(prefixLen);
        }
        // 不属于本前缀的key原样返回
        return fullKey;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.redis.serializer.RedisSerializer#serialize(java.lang.Object)
     */
    @Override
    public byte[] serialize(@Nullable String string) {
        return (string == null ? null :
                (keyprefix + string).getBytes(BtxConsts.DEF_ENCODE));
    }
}
