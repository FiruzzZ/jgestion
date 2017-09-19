package jgestion.jpa.controller;

import jgestion.controller.DAO;
import java.io.Serializable;
import javax.persistence.EntityManager;

public abstract class JGestionJpaImpl<T, ID extends Serializable> extends AbstractDAO<T, ID> {

    protected EntityManager entityManager;

    public JGestionJpaImpl() {
    }

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public void loadLazies(T o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
