package jpa.controller;

import controller.DAO;
import entity.ChequePropio;
import entity.CuentabancariaMovimientos;
import entity.CuentabancariaMovimientos_;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaMovimientosJpaController extends AbstractDAO<CuentabancariaMovimientos, Integer>  {

    private EntityManager entityManager;

    public CuentabancariaMovimientosJpaController() {
        getEntityManager();
    }

    @Override
    protected final EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public CuentabancariaMovimientos findBy(ChequePropio chequePropio) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<CuentabancariaMovimientos> cq = cb.createQuery(getEntityClass());
        Root<CuentabancariaMovimientos> from = cq.from(getEntityClass());
        cq.where(cb.equal(from.get(CuentabancariaMovimientos_.chequePropio), chequePropio));
        return getEntityManager().createQuery(cq).getSingleResult();
    }
}
