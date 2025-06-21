package top.cheesetree.btx.framework.webflux.fastjson;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author van
 * @date 2025/6/19 13:51
 * @description TODO
 */
public class FastJson2Writer implements HttpMessageWriter<Object> {
    static List<MediaType> mediaTypeList = new ArrayList<>() {{
        add(MediaType.APPLICATION_JSON);
    }};

    @Override
    @NonNull
    public List<MediaType> getWritableMediaTypes() {
        return mediaTypeList;
    }

    @Override
    public boolean canWrite(@NonNull ResolvableType elementType, MediaType mediaType) {
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
    public Mono<Void> write(@NonNull Publisher<?> inputStream, @NonNull ResolvableType elementType,
                            MediaType mediaType, @NonNull ReactiveHttpOutputMessage message, @NonNull Map<String,
                    Object> hints) {
        return Mono.from(inputStream).flatMap(is -> {
            message.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            DataBuffer dataBuffer = message.bufferFactory().wrap(JSON.toJSONBytes(is,
                    JSONWriter.Feature.WriteNullStringAsEmpty));
            Flux<DataBuffer> dataStream = Flux.just(dataBuffer);
            return message.writeWith(dataStream);
        });
    }
}
