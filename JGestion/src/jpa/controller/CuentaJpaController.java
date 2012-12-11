package jpa.controller;

import controller.DAO;
import entity.Cuenta;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class CuentaJpaController extends AbstractDAO<Cuenta, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }
}
