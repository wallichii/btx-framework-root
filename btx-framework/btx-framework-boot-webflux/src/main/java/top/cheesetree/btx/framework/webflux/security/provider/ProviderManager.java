package top.cheesetree.btx.framework.webflux.security.provider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.cheesetree.btx.framework.core.constants.BtxMessage;
import top.cheesetree.btx.framework.webflux.security.core.authentication.AuthenticationManager;
import top.cheesetree.btx.framework.webflux.security.core.authentication.AuthenticationProvider;
import top.cheesetree.btx.framework.webflux.security.core.comm.AuthenticationException;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationToken;
import top.cheesetree.btx.framework.webflux.security.exception.ProviderNotFoundException;

import java.util.List;

/**
 * @author van
 * @date 2025/6/24 14:54
 * @description TODO
 */
@Slf4j
public class ProviderManager implements AuthenticationManager {
    @Getter
    private List<AuthenticationProvider> providers;

    public ProviderManager(List<AuthenticationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public AuthenticationInfo authenticate(AuthenticationToken authentication) throws AuthenticationException {
        Class<? extends AuthenticationToken> toTest = authentication.getClass();
        AuthenticationInfo result = null;
        AuthenticationException lastException = null;

        for (AuthenticationProvider provider : this.getProviders()) {
            if (provider.supports(toTest)) {
                try {
                    result = provider.authenticate(authentication);
                    if (result != null) {
                        break;
                    }
                } catch (AuthenticationException ex) {
                    lastException = ex;
                }
            }
        }

        if (result != null) {
            return result;
        } else {
            if (lastException == null) {
                lastException = new ProviderNotFoundException(String.format("ProviderManager.providerNotFound: No " +
                        "AuthenticationProvider found for %s", toTest.getName()),
                        BtxMessage.BUSI_ERROR.getCode().toString());
            }
            throw lastException;
        }
    }

}
