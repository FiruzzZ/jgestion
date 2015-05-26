package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.NotaCreditoProveedor;
import jgestion.entity.Proveedor;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class NotaCreditoProveedorJpaController extends AbstractDAO<NotaCreditoProveedor, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public NotaCreditoProveedor findBy(long numero, Proveedor proveedor) {
        try {
            return (NotaCreditoProveedor) getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
                    + "WHERE o.numero = " + numero + " AND o.proveedor.id=" + proveedor.getId()).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
