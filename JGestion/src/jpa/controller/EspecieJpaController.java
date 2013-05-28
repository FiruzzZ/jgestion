package jpa.controller;

import controller.DAO;
import entity.Especie;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class EspecieJpaController extends AbstractDAO<Especie, Integer> {

    private EntityManager entityManager;

    public EspecieJpaController() {
    }

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }
}
