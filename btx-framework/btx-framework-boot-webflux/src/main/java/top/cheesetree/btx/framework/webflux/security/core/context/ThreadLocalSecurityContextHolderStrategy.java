package top.cheesetree.btx.framework.webflux.security.core.context;

import org.springframework.util.Assert;

/**
 * @author van
 * @date 2025/7/16 16:33
 * @description TODO
 */
final class ThreadLocalSecurityContextHolderStrategy implements SecurityContextHolderStrategy {
    private static final ThreadLocal<SecurityContextImpl> contextHolder = new ThreadLocal<>();

    public void clearContext() {
        contextHolder.remove();
    }

    public SecurityContextImpl getContext() {
        SecurityContextImpl ctx = contextHolder.get();
        if (ctx == null) {
            ctx = this.createEmptyContext();
            contextHolder.set(ctx);
        }

        return ctx;
    }

    public void setContext(SecurityContextImpl context) {
        Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
        contextHolder.set(context);
    }

    public SecurityContextImpl createEmptyContext() {
        return new SecurityContextImpl();
    }

}
