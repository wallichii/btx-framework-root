package top.cheesetree.btx.framework.webflux.security.core.context;

/**
 * @author van
 * @date 2025/7/16 16:31
 * @description TODO
 */
public class SecurityContextHolder {
    private static SecurityContextHolderStrategy strategy;

    private static void initialize() {
        initializeStrategy();
    }

    private static void initializeStrategy() {
        strategy = new ThreadLocalSecurityContextHolderStrategy();
    }


    public static void clearContext() {
        strategy.clearContext();
    }

    public static SecurityContextImpl getContext() {
        return strategy.getContext();
    }

    public static void setContext(SecurityContextImpl context) {
        strategy.setContext(context);
    }


    public static SecurityContextHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

    public static SecurityContextImpl createEmptyContext() {
        return strategy.createEmptyContext();
    }



    static {
        initialize();
    }
}
