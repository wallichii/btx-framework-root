package top.cheesetree.btx.project.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author van
 */
@SpringBootApplication
@ComponentScans({@org.springframework.context.annotation.ComponentScan(value = {"top.cheesetree.btx"})})
@EnableTransactionManagement
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    //TODO 调整tomcat/nginx下umi 问题
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return (factory -> {
            ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/pages/index.html");
            factory.addErrorPages(errorPage404);
        });
    }
}
