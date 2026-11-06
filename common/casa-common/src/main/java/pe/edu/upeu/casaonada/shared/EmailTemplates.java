package pe.edu.upeu.casaonada.shared;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.web.util.HtmlUtils;

/** Plantillas HTML de los correos de CASAS (estilos en linea para compatibilidad con los clientes de correo). */
public final class EmailTemplates {
  private EmailTemplates() {}

  private static final String SITE = "http://localhost:4300";
  private static final Locale ES = Locale.forLanguageTag("es-PE");

  private static String e(String s) {
    return HtmlUtils.htmlEscape(s == null ? "" : s);
  }

  private static String money(BigDecimal v) {
    return "S/ " + NumberFormat.getIntegerInstance(ES).format(v == null ? BigDecimal.ZERO : v);
  }

  private static String layout(String preheader, String title, String body, String ctaText, String ctaUrl) {
    return """
      <!doctype html><html lang="es"><body style="margin:0;background:#f6f4ee;font-family:Arial,Helvetica,sans-serif;color:#18282a">
      <span style="display:none;opacity:0;height:0;overflow:hidden">%s</span>
      <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f6f4ee;padding:28px 12px"><tr><td align="center">
        <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;background:#ffffff;border-radius:18px;overflow:hidden;border:1px solid #e5e1d6">
          <tr><td style="background:#0f4c4a;padding:26px 32px"><span style="font-family:Georgia,serif;font-size:26px;letter-spacing:4px;color:#f0d9a3">CASAS</span><br><span style="font-size:10px;letter-spacing:3px;color:#9fd0cb">CASA O NADA</span></td></tr>
          <tr><td style="padding:34px 32px 8px"><h1 style="margin:0 0 14px;font-family:Georgia,serif;font-weight:400;font-size:28px;line-height:1.2;color:#0f4c4a">%s</h1>%s</td></tr>
          <tr><td style="padding:8px 32px 34px"><a href="%s" style="display:inline-block;background:#0f4c4a;color:#ffffff;text-decoration:none;font-weight:bold;padding:14px 28px;border-radius:999px">%s</a></td></tr>
          <tr><td style="background:#faf8f3;padding:20px 32px;font-size:12px;line-height:1.6;color:#667778;border-top:1px solid #e5e1d6">Recibes este correo porque realizaste una acción en CASAS. Si no fuiste tú, ignora este mensaje.<br>© 2026 CASAS · Proyecto académico del Grupo 9 · Los pagos y contratos de este entorno son de demostración.</td></tr>
        </table>
      </td></tr></table></body></html>
      """.formatted(e(preheader), e(title), body, ctaUrl, e(ctaText));
  }

  private static String p(String html) {
    return "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;color:#34484a\">" + html + "</p>";
  }

  private static String row(String k, String v) {
    return "<tr><td style=\"padding:10px 16px;color:#667778;font-size:14px;border-bottom:1px solid #efece3\">" + e(k)
        + "</td><td style=\"padding:10px 16px;font-size:14px;font-weight:bold;text-align:right;border-bottom:1px solid #efece3\">" + v + "</td></tr>";
  }

  private static String box(String rows) {
    return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #e5e1d6;border-radius:12px;margin:6px 0 18px\">" + rows + "</table>";
  }

  public static String bienvenida(String nombre) {
    var body = p("Hola <b>" + e(nombre) + "</b>, tu cuenta en CASAS está lista.")
        + p("Desde ahora puedes explorar inmuebles, agendar visitas, reservar con un adelanto y seguir cada paso de tu operación en un solo lugar.")
        + box(row("1. Explora", "Compra o alquila") + row("2. Visita (opcional)", "Con un agente") + row("3. Reserva", "Aparta el inmueble 24 horas") + row("4. Paga y verifica", "Yape, Plin o transferencia") + row("5. Contrato", "Revisa y acepta en línea"));
    return layout("Tu cuenta CASAS está lista", "¡Bienvenido a CASAS!", body, "Explorar propiedades", SITE + "/#/propiedades");
  }

  public static String accesoAgente(String nombre, String email) {
    var body = p("Hola <b>" + e(nombre) + "</b>, el administrador creó tu acceso al panel de agentes de CASAS.")
        + box(row("Usuario", e(email)) + row("Contraseña", "La define tu administrador"))
        + p("Desde tu panel podrás publicar inmuebles, atender visitas y revisar los pagos de tus clientes.");
    return layout("Tu acceso de agente a CASAS", "Ya eres parte del equipo CASAS", body, "Ingresar al panel", SITE + "/#/panel");
  }

  public static String reserva(String nombre, String inmueble, String ciudad, BigDecimal adelanto, OffsetDateTime vence, Long reservaId, boolean alquiler) {
    var fecha = vence.atZoneSameInstant(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM, h:mm a", ES));
    var body = p("Hola <b>" + e(nombre) + "</b>, apartamos el inmueble para ti. Tienes tiempo hasta el <b>" + e(fecha) + "</b> para enviar el comprobante de tu adelanto.")
        + box(row("Reserva", "#" + reservaId) + row("Inmueble", e(inmueble)) + row("Ciudad", e(ciudad)) + row("Operación", alquiler ? "Alquiler" : "Compra")
            + row("Adelanto a pagar", money(adelanto)) + row("Vence", e(fecha)))
        + "<p style=\"margin:0 0 6px;font-size:15px;font-weight:bold;color:#0f4c4a\">¿Qué sigue?</p>"
        + p("1. Paga el adelanto con Yape, Plin o transferencia.<br>2. Sube la captura de la operación en tu espacio.<br>3. Un agente verifica tu pago y prepara tu contrato.")
        + p("Si no completas el pago a tiempo, la reserva se cancela sin ningún costo.");
    return layout("Tu reserva está confirmada", "¡Reserva confirmada!", body, "Pagar mi adelanto", SITE + "/#/mi-espacio");
  }
}
