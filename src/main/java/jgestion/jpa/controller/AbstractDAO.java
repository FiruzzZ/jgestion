package jgestion.jpa.controller;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
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
    /**
     * Para cuando se necesita mantener la session abierta.
     * <br>Ej: recuperar objectos en LAZY load; manipular varios JPAControllers simultaneamente;
     */
    private boolean keepItOpen = false;

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

    /**
     * Returns the entity's name alias o
     *
     * @return example {@code MyEntityClassSimpleName o}
     */
    public String getAlias() {
        return getAlias("o");
    }

    public final boolean isKeepItOpen() {
        return keepItOpen;
    }

    public final void setKeepItOpen(boolean keepItOpen) {
        this.keepItOpen = keepItOpen;
    }

    /**
     *
     * @param alias specify an alias for the entity
     * @return a string such {@code MyEntityClassSimpleName} + {@code alias}
     * @see #getAlias()
     */
    public String getAlias(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("alias can't be null");
        }
        return getEntityClass().getSimpleName() + " " + alias + " ";
    }

    /**
     * The initial JPQL statement.
     * <br>Example: {@code SELECT o FROM MyEntityClassSimpleName o }
     * <br><b>No se agrega el <code>WHERE</code> al final por si hay que hacer algún JOIN</b>
     *
     * @return a string such {@code SELECT o FROM MyEntityClassSimpleName o }
     */
    public String getSelectFrom() {
        return getSelectFrom("o");
    }

    /**
     * The initial JPQL statement.
     * <br>Example: {@code SELECT o FROM MyEntityClassSimpleName o }
     *
     * @param alias specify an alias for the entity
     * @return a string such {@code SELECT o FROM MyEntityClassSimpleName} + {@code alias}
     */
    public String getSelectFrom(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("alias can't be null");
        }
        return "SELECT " + alias + " FROM " + getAlias(alias);
    }

    @Override
    public void persist(T entity) {
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
        if (!getEntityManager().getTransaction().isActive()) {
            getEntityManager().getTransaction().begin();
        }
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
        return findAll((Order) null);
//        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
//        cq.select(cq.from(entityClass));
//        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findAll(Order... orders) {
        try {
            CriteriaQuery<T> query = getEntityManager().getCriteriaBuilder().createQuery(entityClass);
            for (Order order : orders) {
                if (order != null) {
                    query.orderBy(order);
                }
            }
            return getEntityManager().createQuery(query).setHint(QueryHints.REFRESH, Boolean.TRUE).getResultList();
        } finally {
            closeEntityManager();
        }
    }

    @Override
    public List<T> findRange(int first, int max) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            cq.select(cq.from(entityClass));
            TypedQuery<T> q = getEntityManager().createQuery(cq);
            q.setMaxResults(max);
            q.setFirstResult(first);
            return q.getResultList();
        } finally {
            closeEntityManager();
        }
    }

    @Override
    public int count() {
        try {
            CriteriaQuery<Long> cq = getEntityManager().getCriteriaBuilder().createQuery(Long.class);
            Root<T> rt = cq.from(entityClass);
            cq.select(getEntityManager().getCriteriaBuilder().count(rt));
            Query q = getEntityManager().createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            closeEntityManager();
        }
    }

    @Override
    public T findByQuery(String query) {
        return findByQuery(query, null);
    }

    /**
     * Similar a {@link #findByQuery(java.lang.String) } pero con retorno flexible!
     *
     * @param query
     * @param parameters
     * @return
     */
    public T findByQuery(String query, Map<String, Object> parameters) {
        try {
            TypedQuery<T> q = getEntityManager().createQuery(query, entityClass);
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    q.setParameter(entry.getKey(), entry.getValue());
                }
            }
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null; //hibenate behaviour huhuhahaha!
        } finally {
            closeEntityManager();
        }
    }

    /**
     * to warranty an unique result this method set the parameter {@link Query#setMaxResults(int)}
     * == 1
     *
     * @param query
     * @return
     */
    public T findByLimitedQuery(String query) {
        return findByLimitedQuery(query, null);
    }

    public T findByLimitedQuery(String query, Map<String, Object> parameters) {
        try {
            Query q = getEntityManager().createQuery(query);
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    q.setParameter(entry.getKey(), entry.getValue());
                }
            }
            return (T) q.setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null; //hibenate behaviour huhuhahaha!
        } finally {
            closeEntityManager();
        }
    }

    @Override
    public List<T> findByNamedQuery(String queryName, Object... params) {
        TypedQuery<T> query = getEntityManager().createNamedQuery(queryName, entityClass);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        List<T> result = query.getResultList();
        return result;
    }

    @Override
    public List<T> findByNamedQueryAndNamedParams(final String name,
            final Map<String, ? extends Object> params) {
        try {
            TypedQuery<T> query = getEntityManager().createNamedQuery(name, entityClass);
            for (final Map.Entry<String, ? extends Object> param : params.entrySet()) {
                query.setParameter(param.getKey(), param.getValue());
            }
            return query.getResultList();
        } finally {
            closeEntityManager();
        }
    }

    /**
     * Executing a native SQL query to return instance(s). (algún día será mas clara esta
     * Javadoc...)
     *
     * @param sqlString a SELECT native SQL statement.
     * @param stringSetMapping
     * @param hints optional hints elements (REFRESH hint will be added if not present)
     * @return a list...
     */
    @SuppressWarnings("unchecked")
    public List<T> findByNativeQuery(String sqlString, String stringSetMapping, Map<String, Object> hints) {
        try {
            Query query;
            if (stringSetMapping == null) {
                query = getEntityManager().createNativeQuery(sqlString, entityClass);
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
        } finally {
            closeEntityManager();
        }
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
     * @param stringSetMapping
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
    public List<T> findAll(String qlString) {
        try {
            TypedQuery<T> typedQuery = getEntityManager().createQuery(qlString, entityClass);
            typedQuery.setHint(QueryHints.REFRESH, Boolean.TRUE);
            return typedQuery.getResultList();
        } finally {
            closeEntityManager();
        }
    }

    public void closeEntityManager() {
        if (!isKeepItOpen()) {
            if (!getEntityManager().isOpen()) {
                getEntityManager().close();
            }
        }
    }

    /**
     * Convenience method to return a single instance that matches the query, or null if the query
     * returns no results.
     *
     * @param jpql
     * @return the single result or null
     */
    public Object findAttribute(String jpql) {
        try {
            return getEntityManager().createQuery(jpql).getSingleResult();
        } catch (NoResultException ex) {
            return null;//same behavior than Hibernate
        } finally {
            closeEntityManager();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findAttributes(String jpql) {
        try {
            return findAttributes(jpql, null, null);
        } finally {
            closeEntityManager();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findAttributes(String jpql, Integer first, Integer max) {
        try {
            Query query = getEntityManager().createQuery(jpql);
            if (first != null) {
                query.setFirstResult(first);
            }
            if (max != null) {
                query.setMaxResults(max);
            }
            return query.getResultList();
        } finally {
            closeEntityManager();
        }
    }

    public final Date getServerDate() {
        try {
            return (Date) getEntityManager().createNativeQuery("SELECT CURRENT_TIMESTAMP").getSingleResult();
        } finally {
            closeEntityManager();
        }
    }

    /**
     * La implementación de este método debe cargar todos los atributos mappeados como LAZY
     *
     * @param o
     */
    public abstract void loadLazies(T o);

    protected Order getOrder(SingularAttribute sa, boolean asc) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        return asc ? criteriaBuilder.asc(getExpression(sa)) : criteriaBuilder.desc(getExpression(sa));
    }

    protected Expression getExpression(SingularAttribute sa) {
        return getEntityManager().getCriteriaBuilder().createQuery(getEntityClass()).from(getEntityClass()).get(sa);
    }
}
