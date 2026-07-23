package top.cheesetree.btx.framework.security.shiro.support.jwt;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.IBtxSecurityPermissionService;
import top.cheesetree.btx.framework.security.IBtxSecurityUserService;
import top.cheesetree.btx.framework.security.model.SecurityFuncDTO;
import top.cheesetree.btx.framework.security.model.SecurityRoleDTO;
import top.cheesetree.btx.framework.security.shiro.config.BtxShiroProperties;
import top.cheesetree.btx.framework.security.shiro.matcher.BtxNoAuthCredentialsMatcher;
import top.cheesetree.btx.framework.security.shiro.model.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static top.cheesetree.btx.framework.security.shiro.constants.BtxSecurityShiroMessage.SECURIT_JWT_ERROR;

/**
 * @author van
 * @date 2022/2/21 09:2
 * @description TODO
 */

@Slf4j
public class BtxSecurityJwtAuthorizingRealm extends AuthorizingRealm {
    @Autowired
    @Lazy
    IBtxSecurityPermissionService<? extends BtxShiroSecurityMenuDTO, ? extends BtxShiroSecurityFuncDTO, ?
            extends BtxShiroSecurityRoleDTO> btxSecurityPermissionService;

    @Autowired
    private BtxShiroProperties btxShiroProperties;

    @Autowired
    @Lazy
    private IBtxSecurityUserService<? extends BtxShiroSecurityUserDTO> btxSecurityUserService;

    @Autowired
    BtxShiroJwtProperties btxShiroJwtProperties;

    public BtxSecurityJwtAuthorizingRealm(BtxNoAuthCredentialsMatcher btxNoAuthCredentialsMatcher) {
        super(btxNoAuthCredentialsMatcher);
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token.getClass().isAssignableFrom(JwtToken.class);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        Object principal = principalCollection.getPrimaryPrincipal();
        if (principal instanceof BtxShiroSecurityAuthUserDTO) {
            List<? extends SecurityFuncDTO> funcs;
            List<? extends SecurityRoleDTO> roles;

            BtxShiroSecurityAuthUserDTO u = (BtxShiroSecurityAuthUserDTO) principalCollection.getPrimaryPrincipal();

            if (u.getUser().getFuncs() != null && !u.getUser().getFuncs().isEmpty()) {
                funcs = u.getUser().getFuncs();
            } else {
                funcs = btxSecurityPermissionService.getFunc(u.getUser().getLoginName());
            }

            if (u.getUser().getRoles() != null && !u.getUser().getRoles().isEmpty()) {
                roles = u.getUser().getRoles();
            } else {
                roles = btxSecurityPermissionService.getRole(u.getUser().getLoginName());
            }

            if (funcs != null) {
                Set<String> stringPermissions = new HashSet<>();
                funcs.forEach((SecurityFuncDTO f) -> {
                    stringPermissions.add(f.getFuncCode());
                });
                info.setStringPermissions(stringPermissions);
            }

            if (roles != null) {
                Set<String> stringRoles = new HashSet<>();
                roles.forEach((SecurityRoleDTO f) -> {
                    stringRoles.add(f.getRoleCode());
                });
                info.setRoles(stringRoles);
            }

        }

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token == null) {
            log.warn("jwt token is null");
            return null;
        }
        JwtToken jwtToken = (JwtToken) token;
        String userid;
        if (StringUtils.hasText(jwtToken.getJwt()) || btxShiroProperties.isIgnoreToken()) {
            if (btxShiroProperties.isIgnoreToken()) {
                userid = btxShiroJwtProperties.getDevUserName();
            } else {
                try {
                    JWT jwt = JWT.of(jwtToken.getJwt());
                    jwt.setKey(btxShiroJwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

                    if ("SM2,SM3,SM4".contains(jwt.getAlgorithm())) {
                        //国密使用自定义 signer
                        jwt.setSigner(jwt.getSigner());
                    }

                    if (jwt.verify()) {
                        userid = jwt.getPayload().getClaim(btxShiroJwtProperties.getSubKey()).toString();
                    } else {
                        throw new AccountException(SECURIT_JWT_ERROR.getMessage());
                    }
                } catch (JWTException | NullPointerException e) {
                    log.error("jwt error:{}", jwtToken.getJwt());
                    throw new AccountException(SECURIT_JWT_ERROR.getMessage());
                }
            }

            CommJSON<? extends BtxShiroSecurityUserDTO> ret =
                    btxSecurityUserService.getUserInfo(userid);
            if (ret.checkSuc()) {
                BtxShiroSecurityAuthUserDTO u = new BtxShiroSecurityAuthUserDTO();
                u.setUser(ret.getResult());
                AuthTokenInfo t = new AuthTokenInfo();
                t.setAccessToken(jwtToken.getJwt());
                u.setAuthinfo(t);
                return new SimpleAuthenticationInfo(new SimplePrincipalCollection(u, "user"),
                        t.getAccessToken());
            } else {
                log.error("get userinfo error:{}", ret.getMsg());
                throw new AccountException(SECURIT_JWT_ERROR.getMessage());
            }
        } else {
            CommJSON<? extends BtxShiroSecurityUserDTO> ret =
                    btxSecurityUserService.login(jwtToken.getUsername(), new String(jwtToken.getPassword()));
            if (ret.checkSuc()) {
                BtxShiroSecurityAuthUserDTO u = new BtxShiroSecurityAuthUserDTO();
                u.setUser(ret.getResult());
                AuthTokenInfo t = new AuthTokenInfo();

                JWT jwt = new JWT();
                jwt.setHeader("alg", btxShiroJwtProperties.getAlgorithmName().toString());

                jwt.setSigner(btxShiroJwtProperties.getAlgorithmName().toString(),
                        btxShiroJwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
                jwt.setExpiresAt(new Date(System.currentTimeMillis() + btxShiroProperties.getSessionTimeOut() * 1000L));
                jwt.setSubject(u.getUser().getLoginName());
                jwt.setIssuedAt(new Date());
                t.setAccessToken(jwt.sign());

                u.setAuthinfo(t);
                return new SimpleAuthenticationInfo(new SimplePrincipalCollection(u, "user"), t.getAccessToken());
            } else {
                throw new AccountException(JSON.toJSONString(ret));
            }

        }
    }


    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        return super.getAuthenticationCacheKey(token);
    }
}
