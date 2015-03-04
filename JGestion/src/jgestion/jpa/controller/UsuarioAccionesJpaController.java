package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.UsuarioAcciones;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class UsuarioAccionesJpaController extends AbstractDAO<UsuarioAcciones, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }
}
