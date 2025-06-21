package top.cheesetree.btx.framework.webflux.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: van
 * @Date: 2022/1/6 16:01
 * @Description: TODO
 */
@ConfigurationProperties("btx.weblux")
@Data
public class BtxWebProperties {
    private String dateformat = "yyyy-MM-dd HH:mm:ss";
    private boolean writedateusedateformat = true;
}
