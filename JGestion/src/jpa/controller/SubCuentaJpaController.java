package jpa.controller;

import controller.DAO;
import entity.SubCuenta;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class SubCuentaJpaController extends AbstractDAO<SubCuenta, Integer> {

    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        if (em == null || !em.isOpen()) {
            em = DAO.getEntityManager();
        }
        return em;
    }
}
