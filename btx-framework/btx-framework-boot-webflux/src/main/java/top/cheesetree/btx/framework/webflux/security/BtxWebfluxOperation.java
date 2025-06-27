package top.cheesetree.btx.framework.webflux.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.cheesetree.btx.framework.core.constants.BtxMessage;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.webflux.security.comm.BtxWebfluxSecurityMessage;
import top.cheesetree.btx.framework.webflux.security.core.IBtxWebfluxOperation;
import top.cheesetree.btx.framework.webflux.security.core.authentication.AuthenticationManager;
import top.cheesetree.btx.framework.webflux.security.core.comm.AuthenticationException;
import top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityEnum;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxCacheProperties;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxSecurityProperties;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;
import top.cheesetree.btx.framework.webflux.security.core.model.SecurityAuthUserDTO;
import top.cheesetree.btx.framework.webflux.security.core.model.StatelessToken;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityAuthUserDTO;

import static top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityConst.CACHE_KEY_PREFIX_TOKEN;

/**
 * @author van
 * @date 2025/6/24 11:14
 * @description TODO
 */
@Component
@Slf4j
public class BtxWebfluxOperation implements IBtxWebfluxOperation {
    @Autowired
    BtxWebfluxSecurityProperties btxWebfluxSecurityProperties;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    BtxWebfluxCacheProperties btxWebfluxCacheProperties;
    @Autowired(required = false)
    BtxWebfluxCacheFactory btxWebfluxCacheFactory;

    @Override
    public CommJSON<WebfluxSecurityAuthUserDTO> login(String... args) {
        CommJSON<WebfluxSecurityAuthUserDTO> ret;

        StatelessToken t = null;

        BtxWebfluxSecurityEnum.AuthType authtype = btxWebfluxSecurityProperties.getAuthType();

        if (args.length > 2 && StringUtils.hasLength(args[2])) {
            authtype = BtxWebfluxSecurityEnum.AuthType.valueOf(args[2]);
        }

        switch (authtype) {
            case JWT:
            case TOKEN:
            default:
                if (args.length > 1) {
                    t = new StatelessToken(args[0], args[1]);
                }
                break;
        }

        try {
            AuthenticationInfo auth = authenticationManager.authenticate(t);
            if (auth.isAuthenticated()) {
                if (btxWebfluxCacheProperties.isEnabled()) {
                    if (btxWebfluxCacheFactory != null) {
                        btxWebfluxCacheFactory.generateCache().add(CACHE_KEY_PREFIX_TOKEN + (String) auth.getCredentials(),
                                (WebfluxSecurityAuthUserDTO) auth.getPrincipals(),
                                btxWebfluxCacheProperties.getCacheExpire());

                        if (log.isDebugEnabled()) {
                            log.debug("security credentials cache key:{} value:{}", auth.getCredentials(),
                                    btxWebfluxCacheFactory.generateCache().get((String) auth.getCredentials()));
                        }
                    } else {
                        log.error("security cache must have one");
                    }
                }

                ret = new CommJSON<>((WebfluxSecurityAuthUserDTO) auth.getPrincipals());
            } else {
                ret = new CommJSON<>(BtxWebfluxSecurityMessage.SECURIT_LOGIN_ERROR);
            }
        } catch (AuthenticationException e) {
            ret = new CommJSON<>(BtxMessage.BUSI_ERROR.getCode(), e.getMessage());
        }

        return ret;
    }

    @Override
    public <T extends SecurityAuthUserDTO> T getAuthInfo() {
        return null;
    }
}
