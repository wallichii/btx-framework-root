package top.cheesetree.btx.framework.web.handle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import top.cheesetree.btx.framework.core.constants.BtxMessage;
import top.cheesetree.btx.framework.core.exception.BtxException;
import top.cheesetree.btx.framework.core.exception.BusinessException;
import top.cheesetree.btx.framework.core.json.CommJSON;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author: van
 * @Date: 2021/8/27 10:05
 * @Description: TODO
 */
@Order()
@ControllerAdvice
@Slf4j
public class BtxWebExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public CommJSON<String> errorHandler(Exception ex) {
        CommJSON<String> ret;

        if (ex instanceof BtxException err) {
            if (err instanceof BusinessException) {
                ret = new CommJSON<>(BtxMessage.BUSI_ERROR.getCode(), err.getErrcode(), err.getMessage(), null);
            } else {
                ret = new CommJSON<>(BtxMessage.SYSTEM_ERROR.getCode(), err.getErrcode(),
                        err.getMessage());
            }

            log.error("系统异常:[{},{}]", err.getErrcode(), err.getMessage(), ex);
        } else {
            ret = new CommJSON<>(BtxMessage.UNKOWN_ERROR);
            String reqid = UUID.randomUUID().toString();
            ret.setMsg(String.format("%s:%s", ret.getMsg(), reqid));
            log.error("系统异常[{}]", reqid, ex);
        }

        return ret;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BindException.class)
    public CommJSON<String> validationErrorHandler(Exception ex) {
        CommJSON<String> ret;

        if (ex instanceof BindException err) {
            List<String> errmsg = new ArrayList<>();
            err.getBindingResult().getFieldErrors().forEach(e -> {
                errmsg.add(e.getDefaultMessage());
            });

            ret = new CommJSON<>(BtxMessage.VALIDATE_ERROR.getCode(), "", StringUtils.collectionToDelimitedString(errmsg, ";"
            ), null);

            log.error("系统异常:[{},{}]", "", err.getMessage(), ex);
        } else {
            ret = new CommJSON<>(BtxMessage.UNKOWN_ERROR);
            String reqid = UUID.randomUUID().toString();
            ret.setMsg(String.format("%s:%s", ret.getMsg(), reqid));
            log.error("系统异常[{}]", reqid, ex);
        }

        return ret;
    }
}
