package top.cheesetree.btx.framework.webflux.security;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.cheesetree.btx.framework.core.constants.BtxMessage;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.IBtxSecurityOperation;
import top.cheesetree.btx.framework.security.constants.BtxSecurityEnum;
import top.cheesetree.btx.framework.security.model.SecurityUserDTO;
import top.cheesetree.btx.framework.webflux.security.comm.BtxWebfluxSecurityMessage;
import top.cheesetree.btx.framework.webflux.security.core.authentication.AuthenticationManager;
import top.cheesetree.btx.framework.webflux.security.core.comm.AuthenticationException;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxCacheProperties;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxSecurityProperties;
import top.cheesetree.btx.framework.webflux.security.core.context.SecurityContextHolder;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;
import top.cheesetree.btx.framework.webflux.security.core.model.StatelessToken;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxAuthTokenInfo;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityAuthUserDTO;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityUserDTO;

import static top.cheesetree.btx.framework.webflux.security.core.comm.BtxWebfluxSecurityConst.CACHE_KEY_PREFIX_TOKEN;

/**
 * @author van
 * @date 2025/6/24 11:14
 * @description TODO
 */
@Component
@Slf4j
@ConditionalOnMissingBean(IBtxSecurityOperation.class)
public class BtxWebfluxOperation<T extends WebfluxSecurityUserDTO, A extends WebfluxAuthTokenInfo> implements IBtxSecurityOperation<T,
        A> {
    @Autowired
    BtxWebfluxSecurityProperties btxWebfluxSecurityProperties;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    BtxWebfluxCacheProperties btxWebfluxCacheProperties;
    @Autowired(required = false)
    BtxWebfluxCacheFactory btxWebfluxCacheFactory;

    @Override
    public CommJSON<WebfluxSecurityAuthUserDTO<T, A>> login(String... args) {
        CommJSON<WebfluxSecurityAuthUserDTO<T, A>> ret;

        StatelessToken t = null;

        BtxSecurityEnum.AuthType authtype = btxWebfluxSecurityProperties.getAuthType();

        if (args.length > 2 && StringUtils.hasLength(args[2])) {
            authtype = BtxSecurityEnum.AuthType.valueOf(args[2]);
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
                        btxWebfluxCacheFactory.generateCache().add(CACHE_KEY_PREFIX_TOKEN + auth.getCredentials(),
                                auth,
                                btxWebfluxCacheProperties.getCacheExpire());

                        if (log.isDebugEnabled()) {
                            log.debug("security credentials cache key:{} value:{}", auth.getCredentials(),
                                    btxWebfluxCacheFactory.generateCache().get((String) auth.getCredentials()));
                        }
                    } else {
                        log.error("security cache must have one");
                    }
                }

                ret = new CommJSON<>((WebfluxSecurityAuthUserDTO<T, A>) auth.getPrincipals());
            } else {
                ret = new CommJSON<>(BtxWebfluxSecurityMessage.SECURIT_LOGIN_ERROR);
            }
        } catch (AuthenticationException e) {
            ret = new CommJSON<>(BtxMessage.BUSI_ERROR.getCode(), e.getMessage());
        }

        return ret;
    }

    @Override
    public CommJSON logout() {
        return null;
    }

    @Override
    public String getUserId() {
        SecurityUserDTO u = getUserInfo();
        if (u != null) {
            return u.getUid();
        } else {
            return null;
        }
    }

    @Override
    public T getUserInfo() {
        Object u = SecurityContextHolder.getContext().getAuthentication().getPrincipals();
        if (u != null) {
            if (u instanceof JSONObject) {
                return JSON.parseObject(u.toString(), new TypeReference<WebfluxSecurityAuthUserDTO<T,
                        WebfluxAuthTokenInfo>>() {
                }).getUser();
            }
            return (T) ((WebfluxSecurityAuthUserDTO) u).getUser();
        } else {
            return null;
        }
    }

    @Override
    public CommJSON runas(T user) {
        return null;
    }

    @Override
    public A getAuthInfo() {
        Object u = SecurityContextHolder.getContext().getAuthentication().getPrincipals();
        if (u != null) {
            return (A) ((WebfluxSecurityAuthUserDTO) u).getAuthinfo();
        } else {
            return null;
        }
    }
}
