package top.cheesetree.btx.framework.webflux.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: van
 * @Date: 2021/8/27 13:58
 * @Description: TODO
 */
@ConfigurationProperties(prefix = "btx.weblux.security")
@Getter
@Setter
public class BtxWebfluxSecurityProperties {
    private List<String> contextInterceptorExcludePathPatterns = new ArrayList<>();
}
