package jpa.controller;

import controller.DAO;
import entity.Sucursal;
import entity.Sucursal_;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author Administrador
 */
public class SucursalJpaController extends AbstractDAO<Sucursal, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    /**
     *
     * @return ordered by {@link Sucursal#nombre}.
     */
    @Override
    public List<Sucursal> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Sucursal> cq = cb.createQuery(getEntityClass());
        Root<Sucursal> from = cq.from(getEntityClass());
        cq.select(from);
        cq.orderBy(cb.asc(from.get(Sucursal_.nombre)));
        return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
    }
}
