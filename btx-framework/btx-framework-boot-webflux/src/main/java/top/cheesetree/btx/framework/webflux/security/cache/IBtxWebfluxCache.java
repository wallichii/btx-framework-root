package top.cheesetree.btx.framework.webflux.security.cache;

/**
 * @author van
 * @date 2022/3/21 15:24
 * @description TODO
 */
public interface IBtxWebfluxCache<K, V> {
    void add(K k, V v, long expire);

    boolean containsKey(K k);

    V get(K k);

    void del(K k);

    IBtxWebfluxCache getInstance();

}
