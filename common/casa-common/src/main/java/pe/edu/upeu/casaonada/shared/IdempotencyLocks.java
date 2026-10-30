package pe.edu.upeu.casaonada.shared;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IdempotencyLocks {
  private final JdbcTemplate db;

  public IdempotencyLocks(JdbcTemplate db) {
    this.db = db;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void lock(String namespace, String key) {
    if (key == null
        || key.isBlank()
        || key.length() > 120
        || key.chars().anyMatch(Character::isISOControl))
      throw new IllegalArgumentException(
          "X-Idempotency-Key debe tener entre 1 y 120 caracteres validos");
    db.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))", namespace + ":" + key);
  }
}
