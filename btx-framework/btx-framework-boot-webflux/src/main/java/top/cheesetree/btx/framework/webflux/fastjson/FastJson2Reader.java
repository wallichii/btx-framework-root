package top.cheesetree.btx.framework.webflux.fastjson;

import com.alibaba.fastjson2.JSON;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.codec.HttpMessageReader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author van
 * @date 2025/6/19 13:51
 * @description TODO
 */
public class FastJson2Reader implements HttpMessageReader<Object> {
    static List<MediaType> mediaTypeList = new ArrayList<>() {{
        add(MediaType.APPLICATION_JSON);
    }};

    @Override
    @NonNull
    public List<MediaType> getReadableMediaTypes() {
        return mediaTypeList;
    }

    @Override
    public boolean canRead(@NonNull ResolvableType elementType, MediaType mediaType) {
        boolean flag = mediaType == null;
        for (MediaType m : mediaTypeList) {
            if (m.includes(mediaType)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    @NonNull
    public Flux<Object> read(@NonNull ResolvableType elementType,
                             @NonNull ReactiveHttpInputMessage message,
                             @NonNull Map<String, Object> hints) {
        return message.getBody()
                .map(dataBuffer -> dataBuffer.toString(StandardCharsets.UTF_8))
                .map(json -> JSON.parseObject(json, elementType.getType()));
    }

    @Override
    @NonNull
    public Mono<Object> readMono(@NonNull ResolvableType elementType,
                                 @NonNull ReactiveHttpInputMessage message,
                                 @NonNull Map<String, Object> hints) {
        return Mono.from(message.getBody())
                .map(dataBuffer -> dataBuffer.toString(StandardCharsets.UTF_8))
                .map(json -> JSON.parseObject(json, elementType.getType()));
    }

}
