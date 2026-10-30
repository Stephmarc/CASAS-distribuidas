package pe.edu.upeu.casaonada.visita;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication;
@org.springframework.context.annotation.Import(pe.edu.upeu.casaonada.shared.CasaInfrastructure.class)
@SpringBootApplication
public class VisitaMsApplication { public static void main(String[] args) { SpringApplication.run(VisitaMsApplication.class,args); } }
