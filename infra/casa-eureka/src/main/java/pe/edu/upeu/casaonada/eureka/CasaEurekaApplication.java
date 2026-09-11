package pe.edu.upeu.casaonada.eureka;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
@SpringBootApplication @EnableEurekaServer public class CasaEurekaApplication { public static void main(String[] a){SpringApplication.run(CasaEurekaApplication.class,a);} }
