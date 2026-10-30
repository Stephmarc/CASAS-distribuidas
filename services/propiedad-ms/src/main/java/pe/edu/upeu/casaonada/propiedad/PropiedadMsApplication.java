package pe.edu.upeu.casaonada.propiedad;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication;
@org.springframework.context.annotation.Import(pe.edu.upeu.casaonada.shared.CasaInfrastructure.class)
@SpringBootApplication
public class PropiedadMsApplication { public static void main(String[] args) { SpringApplication.run(PropiedadMsApplication.class,args); } }
