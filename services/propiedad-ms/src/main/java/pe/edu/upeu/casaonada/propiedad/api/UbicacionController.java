package pe.edu.upeu.casaonada.propiedad.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

/** Non-critical city geocoding: an external outage never blocks a reservation. */
@RestController
@RequestMapping("/api/v1/propiedades/ubicaciones")
public class UbicacionController {
  private final ObjectMapper json;
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
  private record Cached(Instant expires, Map<String,Object> value) {}
  private final Map<String,Cached> cache = new ConcurrentHashMap<>();
  public UbicacionController(ObjectMapper json) { this.json=json; }

  @GetMapping
  public Map<String,Object> buscar(@RequestParam String ciudad) {
    ciudad=ciudad.trim();
    if (!ciudad.matches("[\\p{L} .'-]{2,80}"))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Ciudad no valida");
    String key=ciudad.toLowerCase(Locale.ROOT);
    var old=cache.get(key);
    if(old!=null && old.expires().isAfter(Instant.now())) return old.value();
    try {
      var uri=URI.create("https://geocoding-api.open-meteo.com/v1/search?name="
          +URLEncoder.encode(ciudad,StandardCharsets.UTF_8)+"&count=1&language=es&format=json&countryCode=PE");
      var request=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(4))
          .header("User-Agent","CASAS-Academic/1.0").GET().build();
      var response=http.send(request,HttpResponse.BodyHandlers.ofString());
      if(response.statusCode()!=200) throw new IllegalStateException("Upstream HTTP "+response.statusCode());
      var results=json.readTree(response.body()).path("results");
      if(!results.isArray() || results.isEmpty()) return Map.of("disponible",false,"mensaje","Ciudad sin resultados","proveedor","Open-Meteo / GeoNames");
      var item=results.get(0);
      var result=Map.<String,Object>of("disponible",true,"ciudad",item.path("name").asString(),
          "region",item.path("admin1").asString(),"pais",item.path("country").asString(),
          "latitud",item.path("latitude").asDouble(),"longitud",item.path("longitude").asDouble(),
          "proveedor","Open-Meteo / GeoNames");
      if(cache.size()>200)cache.clear();
      cache.put(key,new Cached(Instant.now().plusSeconds(1800),result));
      org.slf4j.LoggerFactory.getLogger(getClass()).info("EXTERNAL_GEOCODING city={} status=200",ciudad);
      return result;
    } catch (Exception ex) {
      if(ex instanceof InterruptedException)Thread.currentThread().interrupt();
      return Map.of("disponible",false,"mensaje","La ubicacion complementaria no esta disponible temporalmente","proveedor","Open-Meteo / GeoNames");
    }
  }
}
