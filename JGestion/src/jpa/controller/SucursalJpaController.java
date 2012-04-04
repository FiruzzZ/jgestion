package jpa.controller;

import controller.DAO;
import entity.Sucursal;
import entity.Usuario;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author Administrador
 */
public class SucursalJpaController extends AbstractDAO<Sucursal, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }
}
