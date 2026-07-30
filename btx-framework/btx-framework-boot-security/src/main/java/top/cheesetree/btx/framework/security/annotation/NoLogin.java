package top.cheesetree.btx.framework.security.annotation;

/**
 * @author van
 * @date 2026/7/30 15:17
 * @description TODO
 */

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoLogin {
}
