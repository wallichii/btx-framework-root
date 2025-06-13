package top.cheesetree.btx.framework.security.shiro.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.web.servlet.OncePerRequestFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import top.cheesetree.btx.framework.security.shiro.config.BtxShiroCorsProperties;
import top.cheesetree.btx.framework.security.shiro.constants.BtxSecurityShiroConst;

import java.io.IOException;

/**
 * @author van
 * @date 2022/4/28 08:54
 * @description TODO
 */
public class BtxSecurityShiroCorsFilter extends OncePerRequestFilter {
    BtxShiroCorsProperties btxShiroCorsProperties;

    public BtxSecurityShiroCorsFilter(BtxShiroCorsProperties btxShiroCorsProperties) {
        this.btxShiroCorsProperties = btxShiroCorsProperties;
    }

    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        response.setHeader(BtxSecurityShiroConst.XDOMAIN_REQUEST_ALLOWED, "1");//允许COOKIE共享
        if (btxShiroCorsProperties.getOrigins().isEmpty()) {
            response.setHeader(BtxSecurityShiroConst.ACCESS_CONTROL_ALLOW_ORIGIN,
                    request.getHeader(HttpHeaders.ORIGIN));
        } else if (btxShiroCorsProperties.getOrigins().contains(request.getHeader(HttpHeaders.ORIGIN))) {
            response.setHeader(BtxSecurityShiroConst.ACCESS_CONTROL_ALLOW_ORIGIN,
                    request.getHeader(HttpHeaders.ORIGIN));
        }

        response.setHeader(BtxSecurityShiroConst.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                btxShiroCorsProperties.getAllowCredentials());
        //允许请求方式
        response.setHeader(BtxSecurityShiroConst.ACCESS_CONTROL_ALLOW_METHODS,
                btxShiroCorsProperties.getMethods().length > 0 ? String.join(",",
                        btxShiroCorsProperties.getMethods()) : request.getMethod());

        response.setHeader(BtxSecurityShiroConst.ACCESS_CONTROL_ALLOW_HEADERS,
                btxShiroCorsProperties.getAllowHeaders().length > 0 ?
                        String.join(",",
                                btxShiroCorsProperties.getAllowHeaders()) :
                        request.getHeader(BtxSecurityShiroConst.ACCESS_CONTROL_REQUEST_HEADERS
                        ));

        if (RequestMethod.OPTIONS.toString().equals(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return;
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
