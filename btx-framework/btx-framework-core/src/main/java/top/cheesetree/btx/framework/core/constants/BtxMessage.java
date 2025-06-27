package top.cheesetree.btx.framework.core.constants;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: van
 * @Date: 2021/8/25 09:43
 * @Description: TODO
 */
@Data
public class BtxMessage implements Serializable {
    public static final BtxMessage SYSTEM_ERROR = new BtxMessage(-1, "系统错误");
    public static final BtxMessage SUCCESS = new BtxMessage(0, "");
    public static final BtxMessage UNKOWN_ERROR = new BtxMessage(-1, "未知错误");
    public static final BtxMessage BUSI_ERROR = new BtxMessage(1, "业务错误");
    public static final BtxMessage VALIDATE_ERROR = new BtxMessage(1, "校验不通过");
    public static final BtxMessage EXCEPTION_ERROR_ERROR = new BtxMessage(-1, "异常编码生成失败");

    private Integer code;
    private String message;

    public BtxMessage(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BtxMessage() {
    }
}
