package pe.edu.upeu.casaonada.shared;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Envio de correos por SMTP. En desarrollo apunta a Mailpit (bandeja local en http://localhost:28025);
 * con usuario y clave (por ejemplo Gmail con clave de aplicacion) envia correos reales.
 * Nunca lanza excepciones: un fallo de correo no debe romper el registro ni la reserva.
 */
public final class Mailer {
  private static final Logger log = LoggerFactory.getLogger(Mailer.class);
  private final JavaMailSenderImpl sender = new JavaMailSenderImpl();
  private final String from;
  private final boolean enabled;

  public Mailer(String host, int port, String user, String pass, String from, boolean enabled) {
    this.from = from;
    this.enabled = enabled && host != null && !host.isBlank();
    sender.setHost(host);
    sender.setPort(port);
    sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
    Properties p = sender.getJavaMailProperties();
    p.put("mail.smtp.connectiontimeout", "4000");
    p.put("mail.smtp.timeout", "8000");
    p.put("mail.smtp.writetimeout", "8000");
    if (user != null && !user.isBlank()) {
      sender.setUsername(user);
      sender.setPassword(pass);
      p.put("mail.smtp.auth", "true");
      p.put("mail.smtp.starttls.enable", "true");
    }
  }

  /** Envia en segundo plano; devuelve de inmediato. */
  public void send(String to, String subject, String html) {
    if (!enabled || to == null || to.isBlank()) return;
    Thread.ofVirtual().start(() -> {
      try {
        MimeMessage m = sender.createMimeMessage();
        var h = new MimeMessageHelper(m, false, StandardCharsets.UTF_8.name());
        h.setFrom(from);
        h.setTo(to);
        h.setSubject(subject);
        h.setText(html, true);
        sender.send(m);
        log.info("Correo enviado a {}: {}", to, subject);
      } catch (Exception e) {
        log.warn("No se pudo enviar el correo a {}: {}", to, e.getMessage());
      }
    });
  }
}
