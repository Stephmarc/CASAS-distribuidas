package pe.edu.upeu.casaonada.contrato;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication;
@org.springframework.context.annotation.Import(pe.edu.upeu.casaonada.shared.CasaInfrastructure.class)
@SpringBootApplication
public class ContratoMsApplication { public static void main(String[] args) { SpringApplication.run(ContratoMsApplication.class,args); } }
