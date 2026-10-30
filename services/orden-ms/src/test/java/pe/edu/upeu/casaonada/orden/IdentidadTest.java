package pe.edu.upeu.casaonada.orden;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upeu.casaonada.shared.Identidad;
import java.util.List;
class IdentidadTest {
 void login(String role,String id){var jwt=Jwt.withTokenValue("test").header("alg","RS256").subject("test").claim("cliente_id",id).build();SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,List.of(new SimpleGrantedAuthority("ROLE_"+role))));}
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void clienteNoPuedeSuplantarId(){login("CLIENTE","1");assertEquals(1L,Identidad.efectivo(99L));}
 @Test void clienteNoLeeOtraOperacion(){login("CLIENTE","1");assertThrows(AccessDeniedException.class,()->Identidad.propietario(2L));}
 @Test void clienteLeeSuOperacion(){login("CLIENTE","1");assertDoesNotThrow(()->Identidad.propietario(1L));}
 @Test void administradorPuedeGestionarCliente(){login("ADMIN","1");assertDoesNotThrow(()->Identidad.propietario(2L));assertEquals(2L,Identidad.efectivo(2L));}
 @Test void claimInvalidoFallaCerrado(){login("CLIENTE","invalido");assertThrows(AccessDeniedException.class,Identidad::cliente);}
}
