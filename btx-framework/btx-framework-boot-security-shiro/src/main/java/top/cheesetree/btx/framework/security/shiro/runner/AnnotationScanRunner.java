package top.cheesetree.btx.framework.security.shiro.runner;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.cheesetree.btx.framework.security.annotation.NoLogin;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author van
 * @date 2026/7/30 14:44
 * @description TODO
 */
@Component
@Order(value = 100)
@Slf4j
public class AnnotationScanRunner implements ApplicationRunner {
    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    // 缓存所有匿名放行路径（线程安全）
    private static final Set<String> ANONYMOUS_PATH_SET = ConcurrentHashMap.newKeySet();

    private static final AntPathMatcher SPRING_ANT_MATCHER = new AntPathMatcher();

    @Override
    public void run(ApplicationArguments args) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodMap.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // 判断类上 || 方法上是否存在@Anonymous
            boolean hasAnonymous = handlerMethod.hasMethodAnnotation(NoLogin.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(NoLogin.class);
            if (!hasAnonymous) {
                continue;
            }

            // 获取该接口所有path模板
            Set<String> patterns = mappingInfo.getPatternsCondition().getPatterns();
            ANONYMOUS_PATH_SET.addAll(patterns);
        }
    }

    public static boolean checkAnonymousPath(String path) {
        boolean ret = false;

        // 判断当前路径是否为注解免登接口
        for (String pattern : AnnotationScanRunner.ANONYMOUS_PATH_SET) {
            if (SPRING_ANT_MATCHER.match(pattern, path)) {
                ret = true;
                break;
            }
        }

        return ret;
    }
}
