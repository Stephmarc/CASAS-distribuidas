package pe.edu.upeu.casaonada.propiedad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.casaonada.propiedad.entity.OutboxEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

    @Modifying
    @Transactional
    @Query("update OutboxEvent e set e.publishedAt = :publishedAt where e.id = :id and e.publishedAt is null")
    int marcarPublicado(@Param("id") UUID id, @Param("publishedAt") Instant publishedAt);
}
