package top.cheesetree.btx.framework.security.shiro.support.cas;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import top.cheesetree.btx.framework.core.constants.BtxConsts;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.constants.BtxSecurityMessage;
import top.cheesetree.btx.framework.security.shiro.constants.BtxSecurityShiroConst;
import top.cheesetree.btx.framework.web.util.RequestUtil;
import static top.cheesetree.btx.framework.security.shiro.constants.BtxSecurityShiroConst.SKIP_SHIRO_AUTH;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * @author van
 * @date 2022/2/21 10:14
 * @description TODO
 */
@Slf4j
public class BtxSecurityShiroCasFilter extends AuthenticatingFilter {
    private final String loginurl;
    private final String errorurl;

    private final Boolean skipTicketValidation;

    public BtxSecurityShiroCasFilter(String loginurl, String errorurl, Boolean skipTicketValidation) {
        this.loginurl = loginurl;
        this.errorurl = errorurl;
        this.skipTicketValidation = skipTicketValidation;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse servletResponse) {
        return new CasToken(getTicket((HttpServletRequest) request), null);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 读取前置过滤器写入的免登标记
        Object skipFlag = request.getAttribute(SKIP_SHIRO_AUTH);
        if (Boolean.TRUE.equals(skipFlag)) {
            log.debug("[SkipAuthc] 识别免登标记，跳过登录校验");
            return true;
        }
        // 执行原生登录认证逻辑
        return super.isAccessAllowed(request, response, mappedValue);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        if (RequestUtil.isAjaxRequest(req) && !skipTicketValidation) {
            HttpServletResponse rep = (HttpServletResponse) response;
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            rep.setContentType(MediaType.APPLICATION_JSON_VALUE);
            rep.setCharacterEncoding(BtxConsts.DEF_ENCODE.toString());
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(JSON.toJSONBytes(new CommJSON(BtxSecurityMessage.SECURIT_UNLOGIN_ERROR),
                    SerializerFeature.WriteMapNullValue));
            return false;
        } else {
            return this.executeLogin(request, response);
        }
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
                                     ServletResponse response) throws Exception {
        issueSuccessRedirect(request, response);
        return false;
    }

    /**
     * If login has failed, redirect user to the CAS error page (no ticket or ticket validation failed) except if the
     * user is already
     * authenticated, in which case redirect to the default success url.
     *
     * @param token    the token representing the current authentication
     * @param ae       the current authentication exception
     * @param request  the incoming request
     * @param response the outgoing response
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request,
                                     ServletResponse response) {
        // is user authenticated or in remember me mode ?
        Subject subject = getSubject(request, response);
        if (subject.isAuthenticated() || subject.isRemembered() || skipTicketValidation) {
            try {
                log.debug("Redirect to the default success url");
                issueSuccessRedirect(request, response);
            } catch (Exception e) {
                log.error("Cannot redirect to the default success url", e);
            }
        } else {
            log.debug("cas check error");

            String url = null;
            try {
                HttpServletRequest req = (HttpServletRequest) request;
                StringBuffer reqUrlStr = req.getRequestURL();
                String errmsg = StringUtils.hasLength(ae.getMessage()) ? ae.getMessage() :
                        BtxSecurityMessage.SECURIT_CAS_TICKET_ERROR.getMessage();

                if (StringUtils.hasLength(req.getQueryString()) && req.getQueryString().contains(BtxSecurityShiroConst.TICKET_PARAMETER)) {
                    if (StringUtils.hasLength(errorurl)) {
                        url = String.format("%s%serrmsg=%s", errorurl, errorurl.contains("?") ? "&" : "?",
                                URLEncoder.encode(errmsg, BtxConsts.DEF_ENCODE.toString()));
                        ((HttpServletResponse) response).sendRedirect(url);
                    } else {
                        log.debug("Return error message : {}", errmsg);
                        HttpServletResponse rep = (HttpServletResponse) response;
                        rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        rep.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        rep.setCharacterEncoding(BtxConsts.DEF_ENCODE.toString());
                        OutputStream outputStream = response.getOutputStream();
                        outputStream.write(JSON.toJSONBytes(new CommJSON(BtxSecurityMessage.SECURIT_CAS_TICKET_ERROR.getCode(), errmsg),
                                SerializerFeature.WriteMapNullValue));
                    }
                } else {
                    if (StringUtils.hasLength(req.getQueryString())) {
                        reqUrlStr.append("?").append(req.getQueryString());
                    }
                    url = String.format("%s?service=%s", loginurl,
                            URLEncoder.encode(reqUrlStr.toString(),
                                    BtxConsts.DEF_ENCODE.toString()));
                    log.debug("Redirect to cas login url : {}", url);
                    this.saveRequest(request);
                    WebUtils.issueRedirect(request, response, url);
                }
            } catch (IOException e) {
                log.error("Cannot redirect to failure url : {}", url, e);
            }
        }
        return false;
    }

    private String getTicket(HttpServletRequest request) {
        return request.getParameter(BtxSecurityShiroConst.TICKET_PARAMETER);
    }
}
