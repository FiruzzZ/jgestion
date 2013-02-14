package jpa.controller;

import controller.DAO;
import entity.Proveedor;
import entity.Proveedor_;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class ProveedorJpaController extends AbstractDAO<Proveedor, Integer> {

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
     *
     * @return a list ordered by {@link Proveedor#nombre}
     */
    @Override
    public List<Proveedor> findAll() {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Proveedor> cq = cb.createQuery(getEntityClass());
            Root<Proveedor> root = cq.from(getEntityClass());
            cq.select(root);
            cq.orderBy(cb.asc(root.get(Proveedor_.nombre)));
            return getEntityManager().createQuery(cq).setHint(QueryHints.REFRESH, true).getResultList();
        } finally {
            closeEntityManager();
        }
    }
}
