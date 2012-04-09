package jpa.controller;

import controller.DAO;
import entity.Producto;
import java.util.List;
import javax.persistence.EntityManager;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author Administrador
 */
public class ProductoJpaController extends AbstractDAO<Producto, Integer> {

    private EntityManager entityManager;

    public ProductoJpaController() {
        getEntityManager();
    }

    @Override
    protected final EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public Producto find(Integer id) {
        Producto find = entityManager.find(getEntityClass(), id);
        entityManager.setProperty(QueryHints.REFRESH, Boolean.TRUE);
        entityManager.refresh(find);
        return find;
    }

}
