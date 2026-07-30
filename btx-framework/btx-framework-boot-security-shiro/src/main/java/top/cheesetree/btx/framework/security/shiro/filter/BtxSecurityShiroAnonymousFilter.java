package top.cheesetree.btx.framework.security.shiro.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import top.cheesetree.btx.framework.security.shiro.runner.AnnotationScanRunner;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static top.cheesetree.btx.framework.security.shiro.constants.BtxSecurityShiroConst.SKIP_SHIRO_AUTH;

/**
 * @author van
 * @date 2026/7/30 14:45
 * @description TODO
 */
@Slf4j
public class BtxSecurityShiroAnonymousFilter extends AccessControlFilter {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private List<String> anonPatterns = Collections.emptyList();

    public void setAnonPatterns(List<String> anonPatterns) {
        this.anonPatterns = anonPatterns;
    }


    public BtxSecurityShiroAnonymousFilter() {
        super();
    }

    public BtxSecurityShiroAnonymousFilter(List<String> anonPatterns) {
        super();
        this.anonPatterns = anonPatterns;
    }

    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = WebUtils.toHttp(request);
        String path = getServletRelativePath(req);

        // 条件1：匹配URL白名单（原anon逻辑）
        boolean matchUrlWhiteList = matchAnonUrl(path);
        // 条件2：匹配@Anonymous注解
        boolean matchAnnotation = AnnotationScanRunner.checkAnonymousPath(path);

        if (matchUrlWhiteList || matchAnnotation) {
            // 设置标记，通知后续authc过滤器直接放行
            request.setAttribute(SKIP_SHIRO_AUTH, Boolean.TRUE);
            log.debug("[AnonymousPre] 免登匹配 path={}, 标记已设置", path);
        } else {
            log.debug("[AnonymousPre] 需要登录 path={}", path);
        }
        return true;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }

    /**
     * URL匹配，等价原生anon ant风格匹配
     */
    private boolean matchAnonUrl(String requestPath) {
        for (String pattern : anonPatterns) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private String getServletRelativePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null || contextPath.isEmpty()) {
            return requestUri;
        }
        // 确保uri是以contextPath开头（正常情况一定满足，防御编码）
        if (requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }
}
