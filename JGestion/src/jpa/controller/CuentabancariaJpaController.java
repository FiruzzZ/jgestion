package jpa.controller;

import controller.DAO;
import entity.enums.CuentaBancaria;
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
