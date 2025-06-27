package top.cheesetree.btx.framework.webflux.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.cheesetree.btx.framework.webflux.security.core.authentication.AuthenticationManager;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxCacheProperties;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxSecurityProperties;
import top.cheesetree.btx.framework.webflux.security.provider.BtxUserAuthenticationProvider;
import top.cheesetree.btx.framework.webflux.security.provider.ProviderManager;

import java.util.ArrayList;

/**
 * @author van
 * @date 2025/6/20 14:13
 * @description TODO
 */
@Configuration
@EnableConfigurationProperties({BtxWebfluxSecurityProperties.class, BtxWebfluxCacheProperties.class})
public class BtxWebfluxSecurityConfiguration {
    @Autowired
    BtxUserAuthenticationProvider provider;

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(new ArrayList<>() {{
            add(provider);
        }});
    }
}
