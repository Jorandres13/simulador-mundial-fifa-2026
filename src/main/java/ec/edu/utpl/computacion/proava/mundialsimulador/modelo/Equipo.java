package ec.edu.utpl.computacion.proava.mundialsimulador.modelo;

import java.util.Objects;

public class Equipo {
    private final int id;
    private final String nombre;
    private final String codigoIso;
    private final Confederacion confederacion;
    private final int rankingFifa;
    private final double puntosFifa;
    private final boolean anfitrion;

    public Equipo(int id, String nombre, String codigoIso, Confederacion confederacion, int rankingFifa, double puntosFifa, boolean anfitrion) {
        this.id = id;
        this.nombre = Objects.requireNonNull(nombre);
        this.codigoIso = Objects.requireNonNull(codigoIso);
        this.confederacion = Objects.requireNonNull(confederacion);
        this.rankingFifa = rankingFifa;
        this.puntosFifa = puntosFifa;
        this.anfitrion = anfitrion;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoIso() {
        return codigoIso;
    }

    public Confederacion getConfederacion() {
        return confederacion;
    }

    public int getRankingFifa() {
        return rankingFifa;
    }

    public double getPuntosFifa() {
        return puntosFifa;
    }

    public boolean isAnfitrion() {
        return anfitrion;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("#%d %s (%s, %.2f pts)",
                rankingFifa, nombre, codigoIso, puntosFifa);
    }
}
