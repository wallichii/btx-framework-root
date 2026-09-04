package top.cheesetree.btx.framework.cache.redis;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * @author van
 */
public class BtxFastJsonRedisSerializer<T extends Object> implements RedisSerializer {
    private Type classType;

    public BtxFastJsonRedisSerializer(Type classType) {
        this.classType = classType;
    }

    public BtxFastJsonRedisSerializer() {
        this.classType = Object.class;
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object, JSONWriter.Feature.NotWriteRootClassName);
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), classType,
                    JSONReader.Feature.FieldBased);
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Class<?> getTargetType() {
        return classType.getClass();
    }

    @Override
    public boolean canSerialize(Class type) {
        return RedisSerializer.super.canSerialize(type);
    }
}
