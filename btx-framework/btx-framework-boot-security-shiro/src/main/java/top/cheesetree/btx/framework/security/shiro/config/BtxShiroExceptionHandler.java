package top.cheesetree.btx.framework.security.shiro.config;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONValidator;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.cheesetree.btx.framework.core.json.CommJSON;
import top.cheesetree.btx.framework.security.constants.BtxSecurityMessage;

/**
 * @Author: van
 * @Date: 2021/8/27 10:05
 * @Description: TODO
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class BtxShiroExceptionHandler {
    @Autowired
    HttpServletResponse response;

    @ExceptionHandler({UnauthorizedException.class, AuthorizationException.class})
    public ResponseEntity<CommJSON> authrizeErrorHandler(Exception ex) {
        CommJSON ret = new CommJSON(BtxSecurityMessage.SECURIT_UNAUTH_ERROR);
        if (JSONValidator.from(ex.getMessage()).validate()) {
            ret = JSON.parseObject(ex.getMessage(), CommJSON.class);
        } else {
            ret.setMsg(ex.getMessage());
        }

        return new ResponseEntity<>(ret, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({UnauthenticatedException.class, AuthenticationException.class})
    public ResponseEntity<CommJSON> authenterrorHandler(Exception ex) {
        CommJSON ret = new CommJSON(BtxSecurityMessage.SECURIT_UNLOGIN_ERROR);

        if (JSONValidator.from(ex.getMessage()).validate()) {
            ret = JSON.parseObject(ex.getMessage(), CommJSON.class);
        } else {
            ret.setMsg(ex.getMessage());
        }

        return new ResponseEntity<>(ret, HttpStatus.UNAUTHORIZED);
    }

}
