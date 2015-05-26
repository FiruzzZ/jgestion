package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.ChequePropio;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class ChequePropioJpaController extends AbstractDAO<ChequePropio, Integer> {

    private EntityManager entityManger;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManger == null || !entityManger.isOpen()) {
            entityManger = DAO.getEntityManager();

        }
        return entityManger;
    }
}
