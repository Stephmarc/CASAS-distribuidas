package pe.edu.upeu.casaonada.propiedad.entity;

import jakarta.persistence.*;
import pe.edu.upeu.casaonada.propiedad.enums.EstadoPropiedad;
import pe.edu.upeu.casaonada.propiedad.enums.TipoInmueble;
import pe.edu.upeu.casaonada.propiedad.enums.TipoOperacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "propiedades", indexes = {
    @Index(name = "idx_propiedades_estado", columnList = "estado"),
    @Index(name = "idx_propiedades_ciudad", columnList = "ciudad"),
    @Index(name = "idx_propiedades_operacion", columnList = "tipo_operacion")
})
public class Propiedad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_inmueble", nullable = false, length = 30)
    private TipoInmueble tipoInmueble;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 20)
    private TipoOperacion tipoOperacion;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Column(name = "area_m2", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaM2;

    @Column(nullable = false)
    private Integer dormitorios;

    @Column(nullable = false)
    private Integer banos;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPropiedad estado;

    @Column(name = "propietario_id")
    private Long propietarioId;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaCreacion = ahora;
        fechaActualizacion = ahora;
        if (estado == null) estado = EstadoPropiedad.DISPONIBLE;
        if (dormitorios == null) dormitorios = 0;
        if (banos == null) banos = 0;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TipoInmueble getTipoInmueble() { return tipoInmueble; }
    public void setTipoInmueble(TipoInmueble tipoInmueble) { this.tipoInmueble = tipoInmueble; }
    public TipoOperacion getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(TipoOperacion tipoOperacion) { this.tipoOperacion = tipoOperacion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public BigDecimal getAreaM2() { return areaM2; }
    public void setAreaM2(BigDecimal areaM2) { this.areaM2 = areaM2; }
    public Integer getDormitorios() { return dormitorios; }
    public void setDormitorios(Integer dormitorios) { this.dormitorios = dormitorios; }
    public Integer getBanos() { return banos; }
    public void setBanos(Integer banos) { this.banos = banos; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public EstadoPropiedad getEstado() { return estado; }
    public void setEstado(EstadoPropiedad estado) { this.estado = estado; }
    public Long getPropietarioId() { return propietarioId; }
    public void setPropietarioId(Long propietarioId) { this.propietarioId = propietarioId; }
    public Long getVersion() { return version; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
