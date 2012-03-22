package jpa.controller;

import controller.DAO;
import entity.Producto;
import javax.persistence.EntityManager;

/**
 *
 * @author Administrador
 */
public class ProductoJpaCcontroller extends AbstractDAO<Producto, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }
}
