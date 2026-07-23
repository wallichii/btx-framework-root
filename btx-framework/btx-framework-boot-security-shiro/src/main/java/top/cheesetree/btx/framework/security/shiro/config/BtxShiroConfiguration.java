package top.cheesetree.btx.framework.security.shiro.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;
import top.cheesetree.btx.framework.security.IBtxSecurityPermissionService;
import top.cheesetree.btx.framework.security.config.BtxSecurityProperties;
import top.cheesetree.btx.framework.security.constants.BtxSecurityEnum;
import top.cheesetree.btx.framework.security.model.SecurityFuncDTO;
import top.cheesetree.btx.framework.security.model.SecurityMenuDTO;
import top.cheesetree.btx.framework.security.model.SecurityRoleDTO;
import top.cheesetree.btx.framework.security.shiro.filter.*;
import top.cheesetree.btx.framework.security.shiro.matcher.BtxNoAuthCredentialsMatcher;
import top.cheesetree.btx.framework.security.shiro.pam.BtxModularRealmAuthenticator;
import top.cheesetree.btx.framework.security.shiro.realm.BtxSecurityAuthorizingRealm;
import top.cheesetree.btx.framework.security.shiro.subject.StatelessDefaultSubjectFactory;
import top.cheesetree.btx.framework.security.shiro.support.cas.BtxSecurityCasAuthorizingRealm;
import top.cheesetree.btx.framework.security.shiro.support.cas.BtxSecurityShiroCasFilter;
import top.cheesetree.btx.framework.security.shiro.support.cas.BtxShiroCasProperties;
import top.cheesetree.btx.framework.security.shiro.support.jwt.BtxSecurityJwtAuthorizingRealm;
import top.cheesetree.btx.framework.security.shiro.support.jwt.BtxSecurityShiroJwtFilter;
import top.cheesetree.btx.framework.security.shiro.support.jwt.BtxShiroJwtProperties;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.*;

/**
 * @Author: van
 * @Date: 2022/1/12 13:25
 * @Description: TODO
 */
@Configuration
@EnableConfigurationProperties({BtxShiroProperties.class, BtxShiroCacheProperties.class, BtxShiroCasProperties.class,
        BtxShiroCorsProperties.class, BtxShiroCsrfProperties.class, BtxShiroJwtProperties.class})
@Slf4j
public class BtxShiroConfiguration {
    @Autowired
    @Lazy
    BtxSecurityProperties btxSecurityProperties;
    @Autowired
    @Lazy
    BtxShiroCacheProperties btxShiroCacheProperties;
    @Autowired
    @Lazy
    BtxShiroProperties btxShiroProperties;
    @Autowired
    @Lazy
    BtxShiroCasProperties btxShiroCasProperties;
    @Autowired
    @Lazy
    BtxShiroCorsProperties btxShiroCorsProperties;
    @Autowired
    @Lazy
    BtxShiroCsrfProperties btxShiroCsrfProperties;
    @Autowired
    @Lazy
    IBtxSecurityPermissionService<? extends SecurityMenuDTO, ? extends SecurityFuncDTO, ? extends SecurityRoleDTO> btxSecurityPermissionService;

    /**
     * 开启shiro aop注解支持.
     * 使用代理方式;所以需要开启代码支持;
     */
    @Bean
    @Lazy
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    @Bean(name = "shiroFilterFactoryBean")
    @Lazy
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //设置过滤器
        Map<String, Filter> filterMap = shiroFilterFactoryBean.getFilters();

        filterMap.put("user", new BtxSecurityShiroUserFilter());
        filterMap.put("perms", new BtxSecurityShiroPermissionsFilter());

        // 设置拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        if (btxSecurityProperties.getContextInterceptorExcludePathPatterns() != null) {
            //匿名访问
            Arrays.stream(btxSecurityProperties.getContextInterceptorExcludePathPatterns()).forEach((String extpath) -> {
                filterChainDefinitionMap.put(extpath, "anon");
            });
        }

        //设置登录页面
        if (StringUtils.hasLength(btxSecurityProperties.getLoginPath())) {
            shiroFilterFactoryBean.setLoginUrl(btxSecurityProperties.getLoginPath());
            filterChainDefinitionMap.put(btxSecurityProperties.getLoginPath(), "anon");
        }
        //设置未授权页面
        if (StringUtils.hasLength(btxSecurityProperties.getNoAuthPath())) {
            shiroFilterFactoryBean.setUnauthorizedUrl(btxSecurityProperties.getNoAuthPath());
            filterChainDefinitionMap.put(btxSecurityProperties.getNoAuthPath(), "anon");
        }
        //设置错误页面
        if (StringUtils.hasLength(btxSecurityProperties.getErrorPath())) {
            filterChainDefinitionMap.put(btxSecurityProperties.getErrorPath(), "anon");
        }

        if (StringUtils.hasLength(btxSecurityProperties.getExpirePath())) {
            filterChainDefinitionMap.put(btxSecurityProperties.getExpirePath(), "anon");
        }

        //配置权限自动映射
        if (btxShiroProperties.isAutoPermission()) {
            List<? extends SecurityFuncDTO> funcs = btxSecurityPermissionService.getAllFunc();
            if (funcs != null) {
                for (SecurityFuncDTO func : funcs) {
                    if (StringUtils.hasLength(func.getActionLink())) {
                        filterChainDefinitionMap.put(func.getActionLink(), "perms[" + func.getFuncCode() + "]");
                    }
                }
            }
        }

        switch (btxShiroProperties.getAuthType()) {
            case EXT_TOKEN:
            case TOKEN:
                filterMap.put("authc", new BtxSecurityShiroTokenFilter(btxShiroProperties.getTokenKey(),
                        btxShiroProperties.isIgnoreToken(),
                        btxSecurityProperties.getErrorPath()));
                break;
            case JWT:
                filterMap.put("authc", new BtxSecurityShiroJwtFilter(btxShiroProperties.getTokenKey(),
                        btxShiroProperties.isIgnoreToken(),
                        btxSecurityProperties.getErrorPath()));
                break;
            case CAS:
                filterMap.put("authc", new BtxSecurityShiroCasFilter(btxShiroCasProperties.getServerLoginUrl(),
                        btxSecurityProperties.getErrorPath(), btxShiroCasProperties.getSkipTicketValidation()));
                break;
            case SESSION:
                filterMap.put("authc", new BtxSecurityShiroFormFilter());
            default:
                break;
        }
        filterChainDefinitionMap.put("/**", "authc");

        shiroFilterFactoryBean.setFilters(filterMap);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }

    @Bean
    @Lazy
    public Authenticator authenticator() {
        BtxModularRealmAuthenticator authenticator = new BtxModularRealmAuthenticator();
        authenticator.setAuthenticationStrategy(new FirstSuccessfulStrategy());
        return authenticator;
    }

    @Bean
    @ConditionalOnProperty(value = "btx.security.shiro.cache.cache-type", havingValue = "INNER", matchIfMissing = true)
    public org.apache.shiro.cache.CacheManager shiroCacheManager() {
        return new MemoryConstrainedCacheManager();
    }

    @Bean
    @Lazy
    public DefaultWebSubjectFactory subjectFactory() {
        return new StatelessDefaultSubjectFactory();
    }

    @Bean
    @Lazy
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setGlobalSessionTimeout(btxShiroProperties.getSessionTimeOut() * 1000);

        if (StringUtils.hasText(btxShiroProperties.getCookieName())) {
            sessionManager.setSessionIdCookie(new SimpleCookie(btxShiroProperties.getCookieName()));
        }
        return sessionManager;
    }

    /**
     * 注入 securityManager
     */
    @Bean
    @Lazy
    public SessionsSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        List<Realm> rs = new ArrayList<>();
        rs.add(btxSecurityAuthorizingRealm());
        if (BtxSecurityEnum.AuthType.CAS.equals(btxShiroProperties.getAuthType())) {
            rs.add(btxSecurityCasAuthorizingRealm());
        }

        if (BtxSecurityEnum.AuthType.JWT.equals(btxShiroProperties.getAuthType())) {
            rs.add(btxSecurityJwtAuthorizingRealm());
        }

        securityManager.setAuthenticator(authenticator());
        securityManager.setRealms(rs);

        if (btxShiroCacheProperties.isEnabled()) {
            CacheManager cm = shiroCacheManager();
            securityManager.setCacheManager(cm);
        }

        if (btxShiroProperties.getAuthType() != BtxSecurityEnum.AuthType.SESSION && btxShiroProperties.getAuthType
                () != BtxSecurityEnum.AuthType.CAS) {
            // 禁用session
            DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
            DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
            defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
            subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
            securityManager.setSubjectDAO(subjectDAO);
            securityManager.setSubjectFactory(subjectFactory());
        }

        securityManager.setSessionManager(sessionManager());

        return securityManager;
    }

    @Bean
    @Lazy
    public BtxSecurityAuthorizingRealm btxSecurityAuthorizingRealm() {
        BtxSecurityAuthorizingRealm r = new BtxSecurityAuthorizingRealm(new BtxNoAuthCredentialsMatcher());
        if (btxShiroCacheProperties.isEnabled()) {
            r.setAuthenticationCachingEnabled(true);
            r.setAuthorizationCachingEnabled(true);
            r.setAuthenticationCacheName(btxShiroCacheProperties.getAuthenticationCacheName());
            r.setAuthorizationCacheName(btxShiroCacheProperties.getAuthorizationCacheName());
        } else {
            r.setAuthenticationCachingEnabled(false);
            r.setAuthorizationCachingEnabled(false);
        }

        return r;
    }

    @ConditionalOnProperty(value = "btx.security.shiro.auth-type", havingValue = "CAS")
    @Bean
    public BtxSecurityCasAuthorizingRealm btxSecurityCasAuthorizingRealm() {
        BtxSecurityCasAuthorizingRealm r = new BtxSecurityCasAuthorizingRealm(new BtxNoAuthCredentialsMatcher(),
                btxShiroCasProperties.getServerUrlPrefix(), btxShiroCasProperties.getValidationType(),
                btxShiroCasProperties.getClientHostUrl());
        if (btxShiroCacheProperties.isEnabled()) {
            r.setAuthenticationCachingEnabled(true);
            r.setAuthorizationCachingEnabled(true);
            r.setAuthenticationCacheName(btxShiroCacheProperties.getAuthenticationCacheName());
            r.setAuthorizationCacheName(btxShiroCacheProperties.getAuthorizationCacheName());
        } else {
            r.setAuthenticationCachingEnabled(false);
            r.setAuthorizationCachingEnabled(false);
        }

        return r;
    }

    @ConditionalOnProperty(value = "btx.security.shiro.auth-type", havingValue = "JWT")
    @Bean
    public BtxSecurityJwtAuthorizingRealm btxSecurityJwtAuthorizingRealm() {
        BtxSecurityJwtAuthorizingRealm r = new BtxSecurityJwtAuthorizingRealm(new BtxNoAuthCredentialsMatcher());
        if (btxShiroCacheProperties.isEnabled()) {
            r.setAuthenticationCachingEnabled(true);
            r.setAuthorizationCachingEnabled(true);
            r.setAuthenticationCacheName(btxShiroCacheProperties.getAuthenticationCacheName());
            r.setAuthorizationCacheName(btxShiroCacheProperties.getAuthorizationCacheName());
        } else {
            r.setAuthenticationCachingEnabled(false);
            r.setAuthorizationCachingEnabled(false);
        }

        return r;
    }

    @ConditionalOnProperty(value = "btx.security.shiro.cors.enabled", havingValue = "true")
    @Bean
    public FilterRegistrationBean corsFilterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new BtxSecurityShiroCorsFilter(btxShiroCorsProperties));
        registration.addUrlPatterns("/*");
        registration.setName("CorsFilter");
        registration.setOrder(1);
        return registration;
    }

    @ConditionalOnProperty(value = "btx.security.shiro.csrf.enabled", havingValue = "true")
    @Bean
    public FilterRegistrationBean csrfFilterRegistrationBean() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new BtxSecurityShiroCsrfFilter(btxShiroCsrfProperties));
        registration.setEnabled(true);
        registration.addUrlPatterns("/*");
        return registration;
    }


}
