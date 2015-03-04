package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.Especie;
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
