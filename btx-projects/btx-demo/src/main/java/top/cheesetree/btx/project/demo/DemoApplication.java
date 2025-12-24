package top.cheesetree.btx.project.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author van
 * @date 2025/12/24 15:43
 * @description TODO
 */
@SpringBootApplication
@ComponentScans({@org.springframework.context.annotation.ComponentScan(value = {"top.cheesetree.btx"})})
@EnableTransactionManagement
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}