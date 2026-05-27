package ec.edu.utpl.computacion.proava.mundialsimulador.modelo;

import java.util.Objects;

/**
 * Resultado de un partido simulado. Objeto de valor inmutable.
 *
 * Diseñado para ser SEGURO entre hilos: cada Callable retornará uno
 * de estos sin necesidad de sincronización porque es inmutable.
 *
 * Incluye metadatos útiles para análisis:
 *   - duracionMs: cuánto tardó la simulación (medir paralelismo)
 *   - hiloNombre: qué hilo lo simuló (visualizar concurrencia)
 *   - definidoPorPenales: si en eliminatorias hubo penales
 */
public final class ResultadoPartido {

    private final Equipo equipoLocal;
    private final Equipo equipoVisitante;
    private final int golesLocal;
    private final int golesVisitante;
    private final int golesLocalPenales;       // -1 si no hubo
    private final int golesVisitantePenales;   // -1 si no hubo
    private final Equipo ganador;              // null si empate en grupos
    private final long duracionMs;
    private final String hiloNombre;

    private ResultadoPartido(Builder b) {
        this.equipoLocal = Objects.requireNonNull(b.equipoLocal);
        this.equipoVisitante = Objects.requireNonNull(b.equipoVisitante);
        this.golesLocal = b.golesLocal;
        this.golesVisitante = b.golesVisitante;
        this.golesLocalPenales = b.golesLocalPenales;
        this.golesVisitantePenales = b.golesVisitantePenales;
        this.ganador = b.ganador;
        this.duracionMs = b.duracionMs;
        this.hiloNombre = b.hiloNombre;
    }

    public Equipo getEquipoLocal()         { return equipoLocal; }
    public Equipo getEquipoVisitante()     { return equipoVisitante; }
    public int getGolesLocal()             { return golesLocal; }
    public int getGolesVisitante()         { return golesVisitante; }
    public int getGolesLocalPenales()      { return golesLocalPenales; }
    public int getGolesVisitantePenales()  { return golesVisitantePenales; }
    public Equipo getGanador()             { return ganador; }
    public boolean esEmpate()              { return ganador == null; }
    public boolean definidoPorPenales()    { return golesLocalPenales >= 0; }
    public long getDuracionMs()            { return duracionMs; }
    public String getHiloNombre()          { return hiloNombre; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %d - %d %s",
                equipoLocal.getCodigoIso(), golesLocal,
                golesVisitante, equipoVisitante.getCodigoIso()));
        if (definidoPorPenales()) {
            sb.append(String.format(" (penales %d-%d)",
                    golesLocalPenales, golesVisitantePenales));
        }
        if (ganador != null) {
            sb.append(" → gana ").append(ganador.getCodigoIso());
        } else {
            sb.append(" → empate");
        }
        return sb.toString();
    }

    // ----------------------------------------------------------------
    // Builder: el simulador construye el resultado pieza por pieza
    // ----------------------------------------------------------------

    public static Builder builder(Equipo local, Equipo visitante) {
        return new Builder(local, visitante);
    }

    public static final class Builder {
        private final Equipo equipoLocal;
        private final Equipo equipoVisitante;
        private int golesLocal;
        private int golesVisitante;
        private int golesLocalPenales = -1;
        private int golesVisitantePenales = -1;
        private Equipo ganador;
        private long duracionMs;
        private String hiloNombre;

        private Builder(Equipo local, Equipo visitante) {
            this.equipoLocal = local;
            this.equipoVisitante = visitante;
        }

        public Builder golesLocal(int g)         { this.golesLocal = g; return this; }
        public Builder golesVisitante(int g)     { this.golesVisitante = g; return this; }
        public Builder penales(int local, int vis) {
            this.golesLocalPenales = local;
            this.golesVisitantePenales = vis;
            return this;
        }
        public Builder ganador(Equipo g)         { this.ganador = g; return this; }
        public Builder duracionMs(long ms)       { this.duracionMs = ms; return this; }
        public Builder hiloNombre(String n)      { this.hiloNombre = n; return this; }

        public ResultadoPartido build() {
            return new ResultadoPartido(this);
        }
    }
}
