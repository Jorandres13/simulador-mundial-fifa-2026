package ec.edu.utpl.computacion.proava.mundialsimulador.modelo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa un grupo del Mundial 2026 (A, B, C... L).
 *
 * Un grupo contiene exactamente 4 equipos, ordenados por su posición
 * en el sorteo (1 a 4). La posición importa porque define el calendario
 * de partidos según las reglas FIFA.
 *
 * Inmutable: la lista interna se envuelve con Collections.unmodifiableList
 * para evitar que el llamador la modifique. Esto es fundamental para
 * concurrencia: una vez creado, varios hilos pueden leerlo en paralelo
 * sin riesgo.
 */
public final class Grupo {

    private final int id;
    private final String nombre;             // "A", "B", "C"... "L"
    private final List<Equipo> equipos;      // 4 equipos, en orden 1..4

    public Grupo(int id, String nombre, List<Equipo> equipos) {
        this.id = id;
        this.nombre = Objects.requireNonNull(nombre);

        Objects.requireNonNull(equipos);
        if (equipos.size() != 4) {
            throw new IllegalArgumentException(
                    "Un grupo debe tener exactamente 4 equipos, recibí "
                            + equipos.size());
        }

        // Vista inmutable: el cliente no puede modificar la lista.
        // Nota: si el caller modifica su lista original, esto NO protege.
        // Para protección total: List.copyOf(equipos). Lo dejo así por
        // claridad pedagógica, pero coméntelo con sus alumnos.
        this.equipos = Collections.unmodifiableList(equipos);
    }

    public int getId()              { return id; }
    public String getNombre()       { return nombre; }
    public List<Equipo> getEquipos() { return equipos; }

    /**
     * Devuelve el equipo en la posición indicada (1-4) del sorteo.
     * Útil para generar el calendario según las reglas FIFA.
     */
    public Equipo getEquipoEnPosicion(int posicion) {
        if (posicion < 1 || posicion > 4) {
            throw new IllegalArgumentException(
                    "Posición debe estar entre 1 y 4, recibí " + posicion);
        }
        return equipos.get(posicion - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Grupo that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Grupo " + nombre;
    }
}