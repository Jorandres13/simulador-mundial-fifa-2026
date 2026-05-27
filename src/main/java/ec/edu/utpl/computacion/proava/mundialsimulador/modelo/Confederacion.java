package ec.edu.utpl.computacion.proava.mundialsimulador.modelo;

import java.util.Objects;

public class Confederacion {
    private final int id;
    private final String codigo;
    private final String nombre;

    public Confederacion(int id, String codigo, String nombre) {
        this.id = id;
        this.codigo = Objects.requireNonNullElse(codigo, "código no puede ser null");
        this.nombre = Objects.requireNonNull(nombre, "nombre no puede ser null");
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Confederacion that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return codigo + " (" + nombre + ")";
    }
}