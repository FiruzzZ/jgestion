package jpa.controller;

import controller.DAO;
import java.io.Serializable;
import javax.persistence.EntityManager;

public abstract class JGestionJpaImpl<T, ID extends Serializable> extends AbstractDAO<T, ID> {

    protected EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

}
