package top.cheesetree.btx.framework.security.shiro.filter;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.http.MediaType;
import top.cheesetree.btx.framework.core.constants.BtxConsts;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.constants.BtxSecurityMessage;
import top.cheesetree.btx.framework.web.util.RequestUtil;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

import static top.cheesetree.btx.framework.security.shiro.constants.BtxSecurityShiroConst.SKIP_SHIRO_AUTH;

/**
 * @author van
 * @date 2022/2/11 13:19
 * @description TODO
 */
@Slf4j
public class BtxSecurityShiroFormFilter extends FormAuthenticationFilter {

    public BtxSecurityShiroFormFilter() {
        super();
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
        if (RequestUtil.isAjaxRequest(req)) {
            HttpServletResponse rep = (HttpServletResponse) response;
            rep.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            rep.setContentType(MediaType.APPLICATION_JSON_VALUE);
            rep.setCharacterEncoding(BtxConsts.DEF_ENCODE.toString());
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(JSON.toJSONBytes(new CommJSON(BtxSecurityMessage.SECURIT_UNLOGIN_ERROR),
                    JSONWriter.Feature.WriteMapNullValue));
            return false;
        } else {
            return super.onAccessDenied(request, response);
        }
    }
}
