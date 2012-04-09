package jpa.controller;

import java.util.List;
import java.util.Map;

/**
 * This class define the most common methods used when you works with JPA (like
 * CRUD's methods, find a particular instance, retriving by a JPA, Native or
 * Named query. Relating all methods to the entity type specified by the
 * generic.
 *
 * @param <T> the entity type
 * @param <ID> the primary key type
 * @author FiruzzZ
 * @since 16/06/2009
 */
public interface GenericDAO<T, ID> {

    /**
     * Get the Class of the entity.
     *
     * @return the class
     */
    Class<T> getEntityClass();

    void create(T person);

    T merge(T person);

    void remove(T person);

    void refresh(T entity);

    T find(final ID id);

    /**
     * Load all entities.
     *
     * @return the list of entities
     */
    List<T> findAll();

    List<T> findRange(int first, int max);

    int count();

    /**
     * Find using a named query. The parameters are added in order as they was
     * declared on the namedQuery.
     *
     * @param queryName the name of the query
     * @param params the query parameters
     *
     * @return the list of entities
     */
    List<T> findByNamedQuery(
            final String queryName,
            Object... params);

    /**
     * Find using a named query.
     *
     * @param queryName the name of the query
     * @param params the query parameters
     *
     * @return the list of entities
     */
    List<T> findByNamedQueryAndNamedParams(
            final String queryName,
            final Map<String, ? extends Object> params);

    /**
     * Find using a JPA Query
     *
     * @param query a Java Persistence query string
     * @return a list of instances.
     */
    List<T> findByQuery(String query);
}
