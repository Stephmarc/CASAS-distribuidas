package pe.edu.upeu.casaonada.shared;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class ApiDocumentation {
  @Bean
  OpenAPI casaOpenApi() {
    return new OpenAPI()
        .info(new Info().title("CASA O NADA API").version("0.3.0"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
