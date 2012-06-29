package jpa.controller;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 * Provides an implementation of CRUD and JPA most common methods.
 *
 * @param <T> entity Class type
 * @param <ID> the primary key type
 * @author FiruzzZ
 * @see Facade and Generics Pattern. Wakatta?!
 */
public abstract class AbstractDAO<T, ID extends Serializable> implements GenericDAO<T, ID> {

    private final Class<T> entityClass;

    @SuppressWarnings({"unchecked Type Arguments", "unchecked"})
    public AbstractDAO() {
        entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected abstract EntityManager getEntityManager();
//    protected abstract Session getEntityManager(); //Hibernate
//    protected abstract Session getSession(); //Hibernate

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public void create(T entity) {
        getEntityManager().getTransaction().begin();
        getEntityManager().persist(entity);
        getEntityManager().getTransaction().commit();
    }

    @Override
    public T merge(T entity) {
        getEntityManager().getTransaction().begin();
        entity = getEntityManager().merge(entity);
        getEntityManager().getTransaction().commit();
        return entity;
    }

    @Override
    public void remove(T entity) {
//        getEntityManager().delete(getEntityManager().merge(entity)); //Hibernate
        getEntityManager().getTransaction().begin();
        getEntityManager().remove(getEntityManager().merge(entity));
        getEntityManager().getTransaction().commit();
    }

    @Override
    public void refresh(T entity) {
        getEntityManager().refresh(entity);
    }

    @Override
    public T find(ID id) {
//        return (T) getEntityManager().get(entityClass, id); // Hibernate
        return getEntityManager().find(entityClass, id);
    }

    @Override
    public List<T> findAll() {
        //        Criteria cq = getEntityManager().createCriteria(entityClass);
        //        return cq.list();
        CriteriaQuery<T> cq = getEntityManager().getCriteriaBuilder().createQuery(getEntityClass());
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
    }

    @Override
    public List<T> findRange(int first, int max) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(getEntityClass());
        cq.select(cq.from(entityClass));
        TypedQuery<T> q = getEntityManager().createQuery(cq);
        q.setMaxResults(max);
        q.setFirstResult(first);
        return q.getResultList();
    }

    @Override
    public int count() {
        CriteriaQuery<Long> cq = getEntityManager().getCriteriaBuilder().createQuery(Long.class);
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    @Override
    public List<T> findByNamedQuery(String queryName, Object... params) {
        TypedQuery<T> query = getEntityManager().createNamedQuery(queryName, getEntityClass());
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        List<T> result = query.getResultList();
        return result;
    }

    @Override
    public List<T> findByNamedQueryAndNamedParams(final String name,
            final Map<String, ? extends Object> params) {
        TypedQuery<T> query = getEntityManager().createNamedQuery(name, getEntityClass());

        for (final Map.Entry<String, ? extends Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }

    /**
     * Executing a native SQL query to return instance(s). (algún día será mas
     * clara la esta Javadoc...)
     *
     * @param sqlString a SELECT native SQL statement.
     * @param stringSetMapping
     * @param hints optional hints elements
     * @return a list...
     */
    @SuppressWarnings("unchecked")
    public List<T> findByNativeQuery(String sqlString, String stringSetMapping, Map<String, Object> hints) {
        Query query;
        if (stringSetMapping == null) {
            query = getEntityManager().createNativeQuery(sqlString, getEntityClass());
        } else {
            query = getEntityManager().createNativeQuery(sqlString, stringSetMapping);
        }
        if (hints == null || hints.isEmpty() || !hints.containsKey(QueryHints.REFRESH)) {
            query.setHint(QueryHints.REFRESH, Boolean.TRUE);
        }
        if (hints != null) {
            for (Map.Entry<String, Object> entry : hints.entrySet()) {
                query.setHint(entry.getKey(), entry.getValue());
            }
        }
        return query.getResultList();
    }

    /**
     * This method add the hint ({@link QueryHints#REFRESH}, Boolean.TRUE)
     *
     * @param sqlString a SELECT native SQL statement.
     * @return
     * @see #findByNativeQuery(java.lang.String, java.util.Map)
     */
    public List<T> findByNativeQuery(String sqlString) {
        return findByNativeQuery(sqlString, null, null);
    }

    /**
     * This method add the hint ({@link QueryHints#REFRESH}, Boolean.TRUE)
     *
     * @param sqlString a SELECT native SQL statement.
     * @return
     * @see #findByNativeQuery(java.lang.String, java.util.Map)
     */
    public List<T> findByNativeQuery(String sqlString, String stringSetMapping) {
        return findByNativeQuery(sqlString, stringSetMapping, null);
    }

    /**
     * Execute a Java Persistence query language statement with a REFRESH hint!
     *
     * @param qlString
     * @return a list of the results
     */
    @Override
    public List<T> findByQuery(String qlString) {
        TypedQuery<T> typedQuery = getEntityManager().createQuery(qlString, getEntityClass());
        typedQuery.setHint(QueryHints.REFRESH, Boolean.TRUE);
        return typedQuery.getResultList();
    }
}
