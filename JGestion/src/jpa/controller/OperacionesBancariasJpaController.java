/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jpa.controller;

import controller.DAO;
import entity.OperacionesBancarias;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class OperacionesBancariasJpaController extends AbstractDAO<OperacionesBancarias, Integer> {
    private EntityManager entityManager;

    public OperacionesBancariasJpaController() {
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
