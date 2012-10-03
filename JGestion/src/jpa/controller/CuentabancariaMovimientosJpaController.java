package jpa.controller;

import controller.DAO;
import entity.CuentabancariaMovimientos;
import javax.persistence.EntityManager;

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
}
