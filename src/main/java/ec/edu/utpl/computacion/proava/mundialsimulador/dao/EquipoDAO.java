package ec.edu.utpl.computacion.proava.mundialsimulador.dao;

import ec.edu.utpl.computacion.proava.mundialsimulador.db.ConnectionManager;
import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.Confederacion;
import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.Equipo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipoDAO implements GenericDAO<Equipo, Integer> {
    /**
     * JOIN con confederaciones para construir el objeto Equipo completo
     * (con su Confederacion adentro) en una sola consulta.
     *
     * Esto evita el clásico problema N+1: si trajéramos solo el id de
     * confederación y luego hiciéramos otra consulta por cada equipo
     * para resolverla, terminaríamos haciendo 49 consultas para listar
     * los 48 equipos. Con el JOIN: una sola consulta.
     */
    private static final String SQL_BASE = """
        SELECT
            e.id, e.nombre, e.codigo_iso,
            e.ranking_fifa, e.puntos_fifa, e.es_anfitrion,
            c.id   AS conf_id,
            c.codigo AS conf_codigo,
            c.nombre AS conf_nombre
        FROM equipos e
        JOIN confederaciones c ON e.confederacion_id = c.id
        """;

    private static final String SQL_BUSCAR_POR_ID =
            SQL_BASE + " WHERE e.id = ?";

    private static final String SQL_BUSCAR_POR_CODIGO_ISO =
            SQL_BASE + " WHERE e.codigo_iso = ?";

    private static final String SQL_LISTAR_TODOS =
            SQL_BASE + " ORDER BY e.ranking_fifa";

    private static final String SQL_LISTAR_POR_CONFEDERACION =
            SQL_BASE + " WHERE c.id = ? ORDER BY e.ranking_fifa";

    private static final String SQL_CONTAR =
            "SELECT COUNT(*) FROM equipos";

    @Override
    public Optional<Equipo> buscarPorId(Integer id) {
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Error buscando equipo id=" + id, e);
        }
    }

    /**
     * Búsqueda por código ISO (ej. "ECU", "ARG").
     * Útil para tests y para la interfaz de consola.
     */
    public Optional<Equipo> buscarPorCodigoIso(String codigoIso) {
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_CODIGO_ISO)) {

            ps.setString(1, codigoIso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException(
                    "Error buscando equipo código=" + codigoIso, e);
        }
    }

    @Override
    public List<Equipo> listarTodos() {
        return ejecutarConsultaLista(SQL_LISTAR_TODOS, _ -> {});
    }

    public List<Equipo> listarPorConfederacion(int confederacionId) {
        return ejecutarConsultaLista(
                SQL_LISTAR_POR_CONFEDERACION,
                ps -> ps.setInt(1, confederacionId)
        );
    }

    @Override
    public long contar() {
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_CONTAR);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DAOException("Error contando equipos", e);
        }
    }

    // ----------------------------------------------------------------
    // Métodos auxiliares privados
    // ----------------------------------------------------------------

    /**
     * Patrón "Template Method" simplificado con lambda:
     * centraliza el boilerplate de abrir conexión, ejecutar consulta
     * y manejar excepciones. Reduce la repetición de código.
     */
    private List<Equipo> ejecutarConsultaLista(
            String sql,
            ParameterSetter setter) {

        List<Equipo> resultado = new ArrayList<>();
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw new DAOException("Error en consulta: " + sql, e);
        }
    }

    /** Interfaz funcional interna para parametrizar PreparedStatement. */
    @FunctionalInterface
    private interface ParameterSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    /**
     * Mapea una fila a un Equipo. La Confederacion se construye desde
     * los campos del JOIN, evitando una consulta adicional.
     */
    private Equipo mapear(ResultSet rs) throws SQLException {
        Confederacion conf = new Confederacion(
                rs.getInt("conf_id"),
                rs.getString("conf_codigo"),
                rs.getString("conf_nombre")
        );

        return new Equipo(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("codigo_iso"),
                conf,
                rs.getInt("ranking_fifa"),
                rs.getDouble("puntos_fifa"),
                rs.getBoolean("es_anfitrion")
        );
    }
}
