package pe.edu.upeu.casaonada.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class CasaConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(CasaConfigApplication.class, args);
    }
}
