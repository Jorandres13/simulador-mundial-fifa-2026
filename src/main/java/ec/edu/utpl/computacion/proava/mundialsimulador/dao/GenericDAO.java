package ec.edu.utpl.computacion.proava.mundialsimulador.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {
    Optional<T> buscarPorId(ID id);
    List<T> listarTodos();
    long contar();
}
