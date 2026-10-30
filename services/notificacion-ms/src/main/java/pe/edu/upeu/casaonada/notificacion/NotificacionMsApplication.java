package pe.edu.upeu.casaonada.notificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@org.springframework.context.annotation.Import(pe.edu.upeu.casaonada.shared.CasaInfrastructure.class)
@SpringBootApplication
public class NotificacionMsApplication { public static void main(String[] args) { SpringApplication.run(NotificacionMsApplication.class,args); } }
