package pe.edu.upeu.casaonada.reserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ReservaMsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservaMsApplication.class, args);
    }
}
