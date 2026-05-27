package ec.edu.utpl.computacion.proava.mundialsimulador.simulador;


import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.Equipo;
import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.ResultadoPartido;

/**
 * Contrato para simular un partido entre dos equipos.
 *
 * Las implementaciones DEBEN ser thread-safe: el mismo simulador puede
 * ser invocado por varios hilos en paralelo sin sincronización externa.
 * Esto se logra evitando estado mutable: todo lo que el simulador
 * necesita debe venir por parámetro o ser final.
 *
 * El parámetro permiteEmpate distingue las dos modalidades del Mundial:
 *   - Fase de grupos: empates permitidos (3 pts ganador, 1 pt empate).
 *   - Fase eliminatoria: empate inadmisible, se define por penales.
 */
public interface SimuladorPartido {

    /**
     * Simula un partido entre local y visitante.
     *
     * @param local equipo local
     * @param visitante equipo visitante
     * @param permiteEmpate true para fase de grupos, false para eliminatorias
     * @return el resultado del partido
     */
    ResultadoPartido simular(Equipo local, Equipo visitante, boolean permiteEmpate);
}
