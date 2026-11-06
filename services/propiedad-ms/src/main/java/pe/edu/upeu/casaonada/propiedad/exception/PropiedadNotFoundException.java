package pe.edu.upeu.casaonada.propiedad.exception;

public class PropiedadNotFoundException extends RuntimeException {
    public PropiedadNotFoundException(Long id) {
        super("No existe la propiedad con id " + id);
    }
}
