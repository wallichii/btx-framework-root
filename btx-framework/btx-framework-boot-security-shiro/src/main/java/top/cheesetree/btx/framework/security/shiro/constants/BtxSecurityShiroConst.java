package top.cheesetree.btx.framework.security.shiro.constants;

import top.cheesetree.btx.framework.security.constants.BtxSecurityConst;

/**
 * @author van
 * @date 2022/2/18 08:41
 * @description TODO
 */
public interface BtxSecurityShiroConst extends BtxSecurityConst {
    String AUTH_EXT_PARAMS_KEY = "realm_params";
    String TICKET_PARAMETER = "ticket";

    String XDOMAIN_REQUEST_ALLOWED = "XDomainRequestAllowed";

    String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

    String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    String ACCESS_CONTROL_REQUEST_HEADERS = "access-control-request-headers";

    String SKIP_SHIRO_AUTH = "SKIP_SHIRO_AUTH";

}
