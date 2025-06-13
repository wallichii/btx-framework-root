package top.cheesetree.btx.framework.security.shiro.filter;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.springframework.http.MediaType;
import top.cheesetree.btx.framework.core.constants.BtxConsts;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.constants.BtxSecurityMessage;
import top.cheesetree.btx.framework.web.util.RequestUtil;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author van
 * @date 2022/2/11 13:20
 * @description TODO
 */
public class BtxSecurityShiroPermissionsFilter extends PermissionsAuthorizationFilter {

    public BtxSecurityShiroPermissionsFilter() {
        super();
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (RequestUtil.isAjaxRequest(req)) {
            HttpServletResponse rep = (HttpServletResponse) response;
            rep.setStatus(HttpServletResponse.SC_FORBIDDEN);
            rep.setContentType(MediaType.APPLICATION_JSON_VALUE);
            rep.setCharacterEncoding(BtxConsts.DEF_ENCODE.toString());
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(JSON.toJSONBytes(new CommJSON(BtxSecurityMessage.SECURIT_UNAUTH_ERROR),
                    JSONWriter.Feature.WriteMapNullValue));
            return false;
        } else {
            return super.onAccessDenied(request, response);
        }
    }
}
