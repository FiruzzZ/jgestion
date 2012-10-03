package jpa.controller;

import controller.DAO;
import entity.Cuentabancaria;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaJpaController extends AbstractDAO<Cuentabancaria, Integer> {

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
