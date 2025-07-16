package top.cheesetree.btx.framework.webflux.security.core.context;

/**
 * @author van
 * @date 2025/7/16 16:43
 * @description TODO
 */
public interface SecurityContextHolderStrategy {
    void clearContext();

    SecurityContextImpl getContext();

    void setContext(SecurityContextImpl context);

    SecurityContextImpl createEmptyContext();
}
