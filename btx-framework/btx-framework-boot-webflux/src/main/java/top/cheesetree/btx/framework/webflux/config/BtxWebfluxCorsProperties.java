package top.cheesetree.btx.framework.webflux.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author van
 * @date 2022/4/28 09:20
 * @description TODO
 */
@ConfigurationProperties("btx.webflux.cors")
@Getter
@Setter
public class BtxWebfluxCorsProperties {
    private Boolean enabled = false;
    private List<String> origins = new ArrayList<>();
    private List<String> allowHeaders = new ArrayList<>();
    private List<String> methods = new ArrayList<>();
    private List<String> exposedHeaders = new ArrayList<>();
    String allowCredentials = "true";
    long maxAge = -1;

}
