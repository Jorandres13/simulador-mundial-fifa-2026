package ec.edu.utpl.computacion.proava.mundialsimulador.dao;

import ec.edu.utpl.computacion.proava.mundialsimulador.db.ConnectionManager;
import ec.edu.utpl.computacion.proava.mundialsimulador.modelo.Confederacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfederacionDAO implements GenericDAO<Confederacion, Integer> {
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, codigo, nombre FROM confederaciones WHERE id = ?";

    private static final String SQL_LISTAR_TODOS =
            "SELECT id, codigo, nombre FROM confederaciones ORDER BY codigo";

    private static final String SQL_CONTAR =
            "SELECT COUNT(*) FROM confederaciones";

    @Override
    public Optional<Confederacion> buscarPorId(Integer id) {
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
            throw new DAOException(
                    "Error buscando confederación con id=" + id, e);
        }
    }

    @Override
    public List<Confederacion> listarTodos() {
        List<Confederacion> resultado = new ArrayList<>();
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new DAOException("Error listando confederaciones", e);
        }
    }

    @Override
    public long contar() {
        try (Connection con = ConnectionManager.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_CONTAR);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DAOException("Error contando confederaciones", e);
        }
    }

    /**
     * Mapea una fila del ResultSet al objeto del modelo.
     * Método privado: es un detalle interno del DAO.
     */
    private Confederacion mapear(ResultSet rs) throws SQLException {
        return new Confederacion(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre")
        );
    }
}
