package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.CuentaBancaria;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaJpaController extends AbstractDAO<CuentaBancaria, Integer> {

    private EntityManager entityManager;

    public CuentabancariaJpaController() {
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
