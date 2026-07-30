package top.cheesetree.btx.framework.security.shiro.runner;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.cheesetree.btx.framework.security.annotation.NoLogin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author van
 * @date 2026/7/30 15:38
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

            boolean hasAnonymousAnno =
                    handlerMethod.hasMethodAnnotation(NoLogin.class) || handlerMethod.getBeanType().isAnnotationPresent(NoLogin.class);

            if (!hasAnonymousAnno) {
                continue;
            }

            Set<String> pathPatterns = mappingInfo.getPathPatternsCondition().getPatternValues();
            ANONYMOUS_PATH_SET.addAll(pathPatterns);
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
