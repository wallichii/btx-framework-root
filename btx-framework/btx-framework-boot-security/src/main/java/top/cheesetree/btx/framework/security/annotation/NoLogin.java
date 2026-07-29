package top.cheesetree.btx.framework.security.annotation;

import java.lang.annotation.*;

/**
 * @author van
 * @date 2026/7/28 11:05
 * @description TODO
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoLogin {
}
