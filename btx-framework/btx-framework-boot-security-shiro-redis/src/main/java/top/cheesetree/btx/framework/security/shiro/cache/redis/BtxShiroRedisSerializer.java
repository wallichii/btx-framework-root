package top.cheesetree.btx.framework.security.shiro.cache.redis;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

/**
 * @author van
 * @date 2022/2/18 13:18
 * @description TODO
 */
public class BtxShiroRedisSerializer implements RedisSerializer<Object> {

    static {
        JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("org.apache.shiro.authc");
        JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("top.cheesetree.btx.framework.security.model");
        JSONFactory.getDefaultObjectReaderProvider().addAutoTypeAccept("top.cheesetree.btx.framework.security.shiro.model");
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        } else {
            try {
                return JSON.toJSONBytes(object, JSONWriter.Feature.WriteClassName);
            } catch (Exception var3) {
                throw new SerializationException("Could not serialize: " + var3.getMessage(), var3);
            }
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes != null && bytes.length != 0) {
            try {
                return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), Object.class);
            } catch (Exception var3) {
                throw new SerializationException("Could not deserialize: " + var3.getMessage(), var3);
            }
        } else {
            return null;
        }
    }
}
