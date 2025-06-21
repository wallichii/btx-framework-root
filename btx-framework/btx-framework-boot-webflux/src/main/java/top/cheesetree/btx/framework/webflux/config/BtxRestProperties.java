package top.cheesetree.btx.framework.webflux.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: van
 * @Date: 2022/1/6 16:01
 * @Description: TODO
 */
@ConfigurationProperties("btx.weblux.rest")
@Data
public class BtxRestProperties {
    private int connectTimeOut = 2;

    private int readTimeOut = 10;

    private int writeTimeOut = readTimeOut;

    private int maxPoolIdle = 5;

    private int maxPoolLiveTime = 5;
}
