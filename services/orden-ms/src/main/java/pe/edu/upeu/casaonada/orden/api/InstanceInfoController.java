package pe.edu.upeu.casaonada.orden.api;
import org.springframework.beans.factory.annotation.Value; import org.springframework.core.env.Environment; import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.RequestMapping; import org.springframework.web.bind.annotation.RestController; import java.util.Map;
@RestController @RequestMapping("/api/v1/ordenes")
public class InstanceInfoController {
 private final Environment env; @Value("${spring.application.name}") private String app; public InstanceInfoController(Environment env){this.env=env;}
 @GetMapping("/_instance") public Map<String,Object> instance(){ return Map.of("application",app,"port",env.getProperty("local.server.port","unknown")); }
}
