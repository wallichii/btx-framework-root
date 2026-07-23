package top.cheesetree.btx.framework.security.shiro.support.jwt;


import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import top.cheesetree.btx.framework.core.constants.BtxConsts;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.web.util.RequestUtil;

import java.io.OutputStream;
import java.net.URLEncoder;

import static top.cheesetree.btx.framework.security.constants.BtxSecurityMessage.SECURIT_UNLOGIN_ERROR;

/**
 * @author van
 * @date 2022/2/17 15:09
 * @description TOD
 */
@Slf4j
public class BtxSecurityShiroJwtFilter extends AuthenticatingFilter {
    private String tokenKey;
    private boolean ignoreToken;
    private String errorurl;

    public BtxSecurityShiroJwtFilter(String tokenKey, boolean ignoreToken, String errorurl) {
        this.tokenKey = tokenKey;
        this.ignoreToken = ignoreToken;
        this.errorurl = errorurl;
    }

    @SneakyThrows
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (ignoreToken) {
            return super.executeLogin(request, response);
        } else {
            String token = getToken((HttpServletRequest) request);
            if (token != null) {
                try {
                    JWT jwt = JWT.of(token);
                    return jwt.validate(System.currentTimeMillis()) && executeLogin(request, response);
                } catch (JWTException e) {
                    log.error("jwt valid error:{}", token, e);
                }
            }

            return false;
        }

    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        JwtToken token = null;
        String t = getToken((HttpServletRequest) servletRequest);
        if (ignoreToken || StringUtils.hasLength(t)) {
            token = new JwtToken(t);
        }
        return token;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        if (RequestUtil.isAjaxRequest(req)) {
            HttpServletResponse rep = (HttpServletResponse) response;
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            rep.setContentType(MediaType.APPLICATION_JSON_VALUE);
            rep.setCharacterEncoding(BtxConsts.DEF_ENCODE.toString());
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(JSON.toJSONBytes(new CommJSON(SECURIT_UNLOGIN_ERROR),
                    JSONWriter.Feature.WriteMapNullValue));
        } else {
            String url = String.format("%s%serrmsg=%s", errorurl, errorurl.contains("?") ? "&" : "?",
                    URLEncoder.encode(SECURIT_UNLOGIN_ERROR.getMessage(), BtxConsts.DEF_ENCODE));
            ((HttpServletResponse) response).sendRedirect(url);

            if (url.startsWith("http") || url.startsWith("https")) {
                ((HttpServletResponse) response).sendRedirect(url);
            } else {
                WebUtils.issueRedirect(request, response, url);
            }
        }

        return false;
    }


    private String getToken(HttpServletRequest request) {
        String tk = request.getHeader(tokenKey);
        if (!StringUtils.hasText(tk)) {
            tk = request.getParameter(tokenKey);
        }
        return tk;

    }
}
