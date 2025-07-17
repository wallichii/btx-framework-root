package top.cheesetree.btx.framework.webflux.security.provider;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.cheesetree.btx.framework.core.constants.BtxMessage;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.IBtxSecurityUserService;
import top.cheesetree.btx.framework.security.model.SecurityUserDTO;
import top.cheesetree.btx.framework.webflux.security.core.authentication.AuthenticationProvider;
import top.cheesetree.btx.framework.webflux.security.core.comm.AuthenticationException;
import top.cheesetree.btx.framework.webflux.security.core.config.BtxWebfluxSecurityProperties;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationInfo;
import top.cheesetree.btx.framework.webflux.security.core.model.AuthenticationToken;
import top.cheesetree.btx.framework.webflux.security.core.model.SimpleAuthorizationInfo;
import top.cheesetree.btx.framework.webflux.security.core.model.StatelessToken;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxAuthTokenInfo;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityAuthUserDTO;
import top.cheesetree.btx.framework.webflux.security.model.WebfluxSecurityUserDTO;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * @author van
 * @date 2025/6/24 15:27
 * @description TODO
 */
@Component
@Slf4j
public class BtxUserAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    IBtxSecurityUserService<? extends SecurityUserDTO> webfluxUserService;
    @Autowired
    BtxWebfluxSecurityProperties btxWebfluxSecurityProperties;

    @Override
    public AuthenticationInfo authenticate(AuthenticationToken authentication) throws AuthenticationException {
        String userid = authentication.getPrincipal().toString();
        String pwd = authentication.getCredentials().toString();

        CommJSON<? extends SecurityUserDTO> ret = webfluxUserService.login(userid, pwd);
        if (ret.checkSuc()) {
            WebfluxSecurityAuthUserDTO<WebfluxSecurityUserDTO, WebfluxAuthTokenInfo> u = new WebfluxSecurityAuthUserDTO<>();
            u.setUser((WebfluxSecurityUserDTO)ret.getResult());
            WebfluxAuthTokenInfo t = new WebfluxAuthTokenInfo();
            String tk = "";
            switch (btxWebfluxSecurityProperties.getAuthType()) {
                case TOKEN:
                    tk = UUID.randomUUID().toString();
                    break;
                case JWT:
                    tk = JWT.create().setSigner(JWTSignerUtil.hs256(pwd.getBytes(StandardCharsets.UTF_8))).setExpiresAt(new Date(System.currentTimeMillis() + btxWebfluxSecurityProperties.getTimeOut() * 1000L)).setPayload(
                            "userid", userid).sign();
                    break;
                default:
                    break;
            }

            t.setAccessToken(tk);
            t.setExpires_in(btxWebfluxSecurityProperties.getTimeOut());
            u.setAuthinfo(t);

            return new SimpleAuthorizationInfo(tk, u, true);
        } else {
            throw new AuthenticationException(ret.getMsg(), BtxMessage.BUSI_ERROR.getCode().toString());
        }
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass != null && aClass.isAssignableFrom(StatelessToken.class);
    }
}
