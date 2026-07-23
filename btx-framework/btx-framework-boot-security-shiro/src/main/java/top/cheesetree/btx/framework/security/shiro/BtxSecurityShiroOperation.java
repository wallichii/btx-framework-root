package top.cheesetree.btx.framework.security.shiro;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.IBtxSecurityOperation;
import top.cheesetree.btx.framework.security.constants.BtxSecurityEnum;
import top.cheesetree.btx.framework.security.constants.BtxSecurityMessage;
import top.cheesetree.btx.framework.security.model.SecurityUserDTO;
import top.cheesetree.btx.framework.security.shiro.config.BtxShiroCacheProperties;
import top.cheesetree.btx.framework.security.shiro.config.BtxShiroProperties;
import top.cheesetree.btx.framework.security.shiro.model.AuthTokenInfo;
import top.cheesetree.btx.framework.security.shiro.model.BtxShiroSecurityAuthUserDTO;
import top.cheesetree.btx.framework.security.shiro.model.BtxShiroSecurityUserDTO;
import top.cheesetree.btx.framework.security.shiro.realm.BtxSecurityAuthorizingRealm;
import top.cheesetree.btx.framework.security.shiro.subject.StatelessToken;
import top.cheesetree.btx.framework.security.shiro.support.cas.CasToken;
import top.cheesetree.btx.framework.security.shiro.support.jwt.JwtToken;

/**
 * @Author: van
 * @Date: 2022/1/13 09:29
 * @Description: TODO
 */
@Slf4j
@Component
public class BtxSecurityShiroOperation<T extends BtxShiroSecurityUserDTO, A extends AuthTokenInfo> implements IBtxSecurityOperation<T, A> {
    @Autowired
    BtxShiroProperties btxShiroProperties;
    @Autowired
    BtxShiroCacheProperties btxShiroCacheProperties;

    @Override
    public CommJSON<BtxShiroSecurityAuthUserDTO> login(String... args) {
        CommJSON<BtxShiroSecurityAuthUserDTO> ret;

        AuthenticationToken t = null;
        BtxSecurityEnum.AuthType authtype = btxShiroProperties.getAuthType();

        if (args.length > 2 && StringUtils.hasLength(args[2])) {
            authtype = BtxSecurityEnum.AuthType.valueOf(args[2]);
        }

        switch (authtype) {
            case JWT:
                if (args.length > 1) {
                    t = new JwtToken(args[0], args[1]);
                } else {

                }
                break;
            case CAS:
                if (args.length > 0) {
                    t = new CasToken(null, args[0]);
                } else {

                }
                break;
            case EXT_TOKEN:
                if (args.length > 0) {
                    t = new StatelessToken(args[0]);
                } else {

                }
                break;
            case TOKEN:
            case SESSION:
            default:
                if (args.length > 1) {
                    t = new StatelessToken(args[0], args[1]);
                } else {

                }
                break;
        }

        try {
            SecurityUtils.getSubject().login(t);

            ret = new CommJSON<>((BtxShiroSecurityAuthUserDTO) SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal());
        } catch (AuthenticationException e) {
            log.warn("login error:{}", e);
            String errmsg = e.getMessage();
            if (JSONValidator.from(errmsg).validate()) {
                ret = JSON.parseObject(errmsg, CommJSON.class);
            } else {
                ret = new CommJSON<>(BtxSecurityMessage.SECURIT_LOGIN_ERROR.getCode(), e.getMessage());
            }
        }

        return ret;
    }

    @Override
    public CommJSON logout() {
        SecurityUtils.getSubject().logout();

        return new CommJSON("");
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
        Object u = SecurityUtils.getSubject().getPrincipal();
        if (u != null) {
            return (T) (((BtxShiroSecurityAuthUserDTO) u).getUser());
        } else {
            return null;
        }
    }

    @Override
    public CommJSON runas(T user) {
        Subject subject = SecurityUtils.getSubject();

        BtxShiroSecurityAuthUserDTO au = ((BtxShiroSecurityAuthUserDTO) SecurityUtils.getSubject().getPrincipal());
        RealmSecurityManager rsm = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        BtxSecurityAuthorizingRealm shiroRealm = (BtxSecurityAuthorizingRealm) rsm.getRealms().iterator().next();
        AuthenticationToken tk = null;
        boolean isSession =
                BtxSecurityEnum.AuthType.SESSION.equals(btxShiroProperties.getAuthType()) || BtxSecurityEnum.AuthType.CAS.equals(btxShiroProperties.getAuthType());
        switch (btxShiroProperties.getAuthType()) {
            case CAS:
                break;
            case JWT:
                tk = new JwtToken(((AuthTokenInfo) au.getAuthinfo()).getAccessToken());
            case EXT_TOKEN:
            case TOKEN:
            case SESSION:
            default:
                tk = new StatelessToken(((AuthTokenInfo) au.getAuthinfo()).getAccessToken());
                break;
        }
        if (btxShiroCacheProperties.isEnabled()) {
            shiroRealm.clearUserAuthorization(au, tk);
        }
        au.setUser(user);
        if (isSession) {
            subject.runAs(new SimplePrincipalCollection(au, "user"));
        }

        if (btxShiroCacheProperties.isEnabled()) {
            shiroRealm.setUserAuthenticationCache(au, tk);
        }

        return new CommJSON("");
    }

    @Override
    public A getAuthInfo() {
        Object u = SecurityUtils.getSubject().getPrincipal();
        if (u != null) {
            return (A) (((BtxShiroSecurityAuthUserDTO) u).getAuthinfo());
        } else {
            return null;
        }
    }

}
