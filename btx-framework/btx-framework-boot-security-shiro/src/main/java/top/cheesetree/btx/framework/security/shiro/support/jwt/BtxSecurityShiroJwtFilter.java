package top.cheesetree.btx.framework.security.shiro.support.jwt;


import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import cn.hutool.jwt.JWTPayload;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                    return JWT.of(token).getPayload().getClaimsJson().getDate(JWTPayload.EXPIRES_AT).after(DateUtil.date()) && executeLogin(request, response);
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
                    SerializerFeature.WriteMapNullValue));
        } else {
            String url = String.format("%s%serrmsg=%s", errorurl, errorurl.contains("?") ? "&" : "?",
                    URLEncoder.encode(SECURIT_UNLOGIN_ERROR.getMessage(), BtxConsts.DEF_ENCODE.toString()));

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
