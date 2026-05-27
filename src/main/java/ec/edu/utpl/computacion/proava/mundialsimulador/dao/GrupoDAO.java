package ec.edu.utpl.computacion.proava.mundialsimulador.dao;

import ec.edu.utpl.computacion.proava.mundialsimulador.db.ConnectionManager;
import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.Confederacion;
import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.Equipo;
import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.Grupo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GrupoDAO implements GenericDAO<Grupo, Integer> {

    /**
     * Consulta principal: trae grupos + equipos + confederaciones en
     * una sola consulta, ordenados por grupo y posición de sorteo.
     *
     * Estrategia: como una fila por equipo (no por grupo), el método
     * mapear() reconstruye los grupos agrupando las filas en memoria.
     * Es el patrón clásico para resolver relaciones uno-a-muchos en
     * JDBC puro sin caer en el N+1.
     */
    private static final String SQL_BASE = """
        SELECT
            g.id   AS grupo_id,
            g.nombre AS grupo_nombre,
            eg.posicion_sorteo,
            e.id   AS equipo_id,
            e.nombre AS equipo_nombre,
            e.codigo_iso,
            e.ranking_fifa,
            e.puntos_fifa,
            e.es_anfitrion,
            c.id   AS conf_id,
            c.codigo AS conf_codigo,
            c.nombre AS conf_nombre
        FROM grupos g
        JOIN equipos_grupo eg ON eg.grupo_id = g.id
        JOIN equipos e        ON eg.equipo_id = e.id
        JOIN confederaciones c ON e.confederacion_id = c.id
        """;

    private static final String SQL_BUSCAR_POR_ID =
        SQL_BASE + " WHERE g.id = ? ORDER BY eg.posicion_sorteo";

    private static final String SQL_BUSCAR_POR_NOMBRE =
        SQL_BASE + " WHERE g.nombre = ? ORDER BY eg.posicion_sorteo";

    private static final String SQL_LISTAR_TODOS =
        SQL_BASE + " ORDER BY g.nombre, eg.posicion_sorteo";

    private static final String SQL_CONTAR = "SELECT COUNT(*) FROM grupos";

    @Override
    public Optional<Grupo> buscarPorId(Integer id) {
        try (
            Connection con = ConnectionManager.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)
        ) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<Grupo> resultado = mapearGrupos(rs);
                return resultado.isEmpty()
                    ? Optional.empty()
                    : Optional.of(resultado.get(0));
            }
        } catch (SQLException e) {
            throw new DAOException("Error buscando grupo id=" + id, e);
        }
    }

    /**
     * Búsqueda por nombre del grupo (ej. "A", "B"... "L").
     * Más cómoda que buscar por id para uso humano.
     */
    public Optional<Grupo> buscarPorNombre(String nombre) {
        try (
            Connection con = ConnectionManager.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_NOMBRE)
        ) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                List<Grupo> resultado = mapearGrupos(rs);
                return resultado.isEmpty()
                    ? Optional.empty()
                    : Optional.of(resultado.get(0));
            }
        } catch (SQLException e) {
            throw new DAOException("Error buscando grupo nombre=" + nombre, e);
        }
    }

    @Override
    public List<Grupo> listarTodos() {
        try (
            Connection con = ConnectionManager.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_LISTAR_TODOS);
            ResultSet rs = ps.executeQuery()
        ) {
            return mapearGrupos(rs);
        } catch (SQLException e) {
            throw new DAOException("Error listando grupos", e);
        }
    }

    @Override
    public long contar() {
        try (
            Connection con = ConnectionManager.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_CONTAR);
            ResultSet rs = ps.executeQuery()
        ) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DAOException("Error contando grupos", e);
        }
    }

    // ----------------------------------------------------------------
    // Mapeo: el corazón de este DAO
    // ----------------------------------------------------------------

    /**
     * Convierte un ResultSet con filas equipo-por-equipo en una lista
     * de Grupos, cada uno con sus 4 equipos.
     *
     * Algoritmo:
     *   1. Recorrer todas las filas.
     *   2. Para cada fila, identificar a qué grupo pertenece (por id).
     *   3. Acumular los equipos en una estructura temporal (Map).
     *   4. Al final, construir los objetos Grupo inmutables.
     *
     * Usamos LinkedHashMap para preservar el orden de inserción
     * (los grupos saldrán en el orden del ORDER BY: A, B, C... L).
     */
    private List<Grupo> mapearGrupos(ResultSet rs) throws SQLException {
        // Estructura temporal: id_grupo -> (nombre, equipos acumulados)
        Map<Integer, BuilderGrupo> builders = new LinkedHashMap<>();

        while (rs.next()) {
            int grupoId = rs.getInt("grupo_id");

            // Si es la primera vez que vemos este grupo, creamos su builder
            BuilderGrupo builder = builders.computeIfAbsent(grupoId, id ->
                new BuilderGrupo(id, rsGetGrupoNombre(rs))
            );

            // Mapear el equipo de esta fila y agregarlo al grupo
            builder.equipos.add(mapearEquipo(rs));
        }

        // Construir los Grupos inmutables a partir de los builders
        List<Grupo> resultado = new ArrayList<>(builders.size());
        for (BuilderGrupo b : builders.values()) {
            resultado.add(new Grupo(b.id, b.nombre, b.equipos));
        }
        return resultado;
    }

    // Pequeño helper para manejar la SQLException de getString
    private String rsGetGrupoNombre(ResultSet rs) {
        try {
            return rs.getString("grupo_nombre");
        } catch (SQLException e) {
            throw new DAOException("Error leyendo nombre del grupo", e);
        }
    }

    private Equipo mapearEquipo(ResultSet rs) throws SQLException {
        Confederacion conf = new Confederacion(
            rs.getInt("conf_id"),
            rs.getString("conf_codigo"),
            rs.getString("conf_nombre")
        );
        return new Equipo(
            rs.getInt("equipo_id"),
            rs.getString("equipo_nombre"),
            rs.getString("codigo_iso"),
            conf,
            rs.getInt("ranking_fifa"),
            rs.getDouble("puntos_fifa"),
            rs.getBoolean("es_anfitrion")
        );
    }

    /**
     * Builder mutable interno, usado solo durante el mapeo.
     * No se expone fuera del DAO.
     */
    private static class BuilderGrupo {

        final int id;
        final String nombre;
        final List<Equipo> equipos = new ArrayList<>(4);

        BuilderGrupo(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }
}
