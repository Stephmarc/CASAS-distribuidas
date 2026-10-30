package pe.edu.upeu.casaonada.orden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@org.springframework.context.annotation.Import(pe.edu.upeu.casaonada.shared.CasaInfrastructure.class)
@SpringBootApplication
public class OrdenMsApplication { public static void main(String[] args) { SpringApplication.run(OrdenMsApplication.class,args); } }
