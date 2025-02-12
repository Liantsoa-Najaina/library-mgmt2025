package dao;

import java.sql.SQLException;
import java.util.List;

public interface CrudOperations<E> {
    List<E> getAll(int page, int size);

    List<E> findByCriteria(List<Criteria> criteria);

    List<E> findAllSortedBy(String sortField, String sortDirection, int page, int size);

    E findById(String id);

    // Both create (if does not exist) or update (if exist) entities
    List<E> saveAll(List<E> entities) throws SQLException;

    E update(String id, E updatedEntity);

    boolean deleteById(String id);
}
