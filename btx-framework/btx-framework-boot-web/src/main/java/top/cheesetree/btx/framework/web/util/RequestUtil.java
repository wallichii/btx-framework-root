package top.cheesetree.btx.framework.web.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;


/**
 * @author van
 * @date 2022/2/11 13:54
 * @description TODO
 */
public class RequestUtil {

    /**
     * 是否是ajax异步请求
     *
     * @param request
     * @return
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.indexOf("XMLHttpRequest") != -1) {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.endsWithIgnoreCase(uri, ".json") || StringUtils.endsWithIgnoreCase(uri, ".xml")) {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        if ("json".equalsIgnoreCase(ajax) || "xml".equalsIgnoreCase(ajax)) {
            return true;
        }

        return false;
    }
}
