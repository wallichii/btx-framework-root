package top.cheesetree.btx.framework.security.shiro.filter;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.OncePerRequestFilter;
import org.springframework.http.MediaType;
import top.cheesetree.btx.framework.core.constants.BtxConsts;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.constants.BtxSecurityMessage;
import top.cheesetree.btx.framework.security.shiro.config.BtxShiroCsrfProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author van
 * @date 2025/3/10 10:08
 * @description TODO
 */
public class BtxSecurityShiroCsrfFilter extends OncePerRequestFilter {
    private List<String> domains;

    public BtxSecurityShiroCsrfFilter(BtxShiroCsrfProperties properties) {
        this.domains = properties.getDomains();
    }

    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS").contains(request.getMethod()) || domains.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        // 从 HTTP 头中取得 Referer 值
        String referer = request.getHeader("Referer");
        // 判断 Referer 是否以 合法的域名 开头。
        if (referer != null) {
            if (referer.indexOf("://") > 0) {
                referer.substring(referer.indexOf("://") + 3);
            }
            // 如 mysite.com/abc.html
            if (referer.indexOf("/") > 0) {
                referer.substring(0, referer.indexOf("/"));
            }
            // 如 mysite.com:8080
            if (referer.indexOf(":") > 0) {
                referer.substring(0, referer.indexOf(":"));
            }
            // 如 mysite.com
            for (String domain : domains) {
                if (referer.endsWith(domain)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(BtxConsts.DEF_ENCODE.toString());
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(JSON.toJSONBytes(new CommJSON<String>(BtxSecurityMessage.SECURIT_CSRF_ERROR),
                JSONWriter.Feature.WriteMapNullValue));
    }

    private boolean verifyDomains(HttpServletRequest request) {
        // 从 HTTP 头中取得 Referer 值
        String referer = request.getHeader("Referer");
        // 判断 Referer 是否以 合法的域名 开头。
        if (referer != null) {
            // 如 http://mysite.com/abc.html https://www.mysite.com:8080/abc.html
            if (referer.indexOf("://") > 0) referer = referer.substring(referer.indexOf("://") + 3);
            // 如 mysite.com/abc.html
            if (referer.indexOf("/") > 0) referer = referer.substring(0, referer.indexOf("/"));
            // 如 mysite.com:8080
            if (referer.indexOf(":") > 0) referer = referer.substring(0, referer.indexOf(":"));
            // 如 mysite.com
            for (String domain : domains) {
                if (referer.endsWith(domain)) return true;
            }
        }
        return false;
    }
}
