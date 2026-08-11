package top.cheesetree.btx.framework.security.shiro.realm;


import com.alibaba.fastjson2.JSON;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.lang.util.ByteSource;
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
import top.cheesetree.btx.framework.security.shiro.config.BtxShiroCacheProperties;
import top.cheesetree.btx.framework.security.shiro.config.BtxShiroProperties;
import top.cheesetree.btx.framework.security.shiro.matcher.BtxNoAuthCredentialsMatcher;
import top.cheesetree.btx.framework.security.shiro.model.*;
import top.cheesetree.btx.framework.security.shiro.subject.StatelessToken;
import top.cheesetree.btx.framework.security.shiro.util.BtxSerializableByteSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: van
 * @Date: 2022/1/12 15:15
 * @Description: TODO
 */
public class BtxSecurityAuthorizingRealm extends AuthorizingRealm {
    @Autowired
    @Lazy
    IBtxSecurityPermissionService<? extends BtxShiroSecurityMenuDTO, ? extends BtxShiroSecurityFuncDTO, ?
            extends BtxShiroSecurityRoleDTO> btxSecurityPermissionService;

    @Autowired
    @Lazy
    private IBtxSecurityUserService<? extends BtxShiroSecurityUserDTO> btxSecurityUserService;

    @Autowired
    @Lazy
    private BtxShiroProperties btxShiroProperties;

    @Autowired
    BtxShiroCacheProperties btxShiroCacheProperties;

    public BtxSecurityAuthorizingRealm(BtxNoAuthCredentialsMatcher btxNoAuthCredentialsMatcher) {
        super(btxNoAuthCredentialsMatcher);
        this.setAuthenticationTokenClass(StatelessToken.class);
    }


    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token.getClass().isAssignableFrom(StatelessToken.class);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        BtxShiroSecurityAuthUserDTO u = (BtxShiroSecurityAuthUserDTO) principalCollection.getPrimaryPrincipal();

        List<? extends SecurityFuncDTO> funcs;
        List<? extends SecurityRoleDTO> roles;

        if (u.getUser().getFuncs() != null && !u.getUser().getFuncs().isEmpty()) {
            funcs = u.getUser().getFuncs();
        } else {
            funcs = btxSecurityPermissionService.getFunc(u.getUser().getUid());
        }

        if (u.getUser().getRoles() != null && !u.getUser().getRoles().isEmpty()) {
            roles = u.getUser().getRoles();
        } else {
            roles = btxSecurityPermissionService.getRole(u.getUser().getUid());
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

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        StatelessToken token = (StatelessToken) authenticationToken;

        CommJSON<? extends BtxShiroSecurityUserDTO> ret =
                btxSecurityUserService.login(StringUtils.hasLength(token.getUsername()) ? token.getUsername() :
                                token.getToken(),
                        token.getPassword() == null ? null : new String(token.getPassword()));
        if (ret.checkSuc()) {
            BtxShiroSecurityAuthUserDTO u = new BtxShiroSecurityAuthUserDTO();
            u.setUser(ret.getResult());
            AuthTokenInfo t = new AuthTokenInfo();
            t.setAccessToken(token.getToken());
            u.setAuthinfo(t);

            ByteSource salt = BtxSerializableByteSource.bytes("");
            return new SimpleAuthenticationInfo(new SimplePrincipalCollection(u, "user"), t.getAccessToken(), salt);
        } else {
            throw new AccountException(JSON.toJSONString(ret));
        }

    }

    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        return token != null ? ((StatelessToken) token).getToken() : null;
    }

    public void clearUserAuthorization(BtxShiroSecurityAuthUserDTO u, AuthenticationToken token) {
        this.doClearCache(new SimplePrincipalCollection(u, "user"));

        if (btxShiroCacheProperties.isEnabled()) {
            Cache<Object, AuthorizationInfo> azcache = this.getAuthorizationCache();
            if (azcache != null) {
                azcache.remove(getAuthenticationCacheKey(token));
            }
            Cache<Object, AuthenticationInfo> accache = this.getAuthenticationCache();
            if (accache != null) {
                accache.remove(getAuthenticationCacheKey(token));
            }
        }
    }

    public void setUserAuthenticationCache(BtxShiroSecurityAuthUserDTO u, AuthenticationToken token) {
        Cache<Object, AuthenticationInfo> accache = this.getAuthenticationCache();
        if (accache != null) {
            Object key = this.getAuthenticationCacheKey(token);
            accache.put(key, new SimpleAuthenticationInfo(new SimplePrincipalCollection(u, "user"),
                    token.getPrincipal()));
        }
    }

}
