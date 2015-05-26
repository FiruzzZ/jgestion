package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.CreditoProveedor;
import jgestion.entity.CreditoProveedor_;
import jgestion.entity.Proveedor;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class CreditoProveedorJpaController extends AbstractDAO<CreditoProveedor, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public List<CreditoProveedor> findBy(Proveedor p) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<CreditoProveedor> query = cb.createQuery(getEntityClass());
        Root<CreditoProveedor> root = query.from(getEntityClass());
        query.where(cb.equal(root.get(CreditoProveedor_.proveedor), p));
//        query.orderBy(cb.asc(root.get(CreditoProveedor_.fechaCarga)));
        return getEntityManager().createQuery(query).getResultList();
    }
}
