package pe.edu.upeu.casaonada.shared;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;
@Configuration
@EnableScheduling
@ComponentScan(basePackageClasses=CasaInfrastructure.class)
@EnableFeignClients(basePackageClasses=CasaClients.class)
public class CasaInfrastructure {}
