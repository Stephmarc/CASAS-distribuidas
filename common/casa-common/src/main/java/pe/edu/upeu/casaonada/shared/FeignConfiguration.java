package pe.edu.upeu.casaonada.shared;

import feign.RequestInterceptor;
import org.springframework.context.annotation.*;
import org.springframework.web.context.request.*;
import org.slf4j.MDC;
@Configuration
public class FeignConfiguration {
    @Bean RequestInterceptor casaHeaders(){return request->{
        String trace=MDC.get("traceId"); if(trace!=null)request.header("X-Trace-ID",trace);
        if(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a){String h=a.getRequest().getHeader("Authorization");if(h!=null)request.header("Authorization",h);}
    };}
}
