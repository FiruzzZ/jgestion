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
public class NotaCreditoProveedorJpaController extends JGestionJpaImpl<NotaCreditoProveedor, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public NotaCreditoProveedor findBy(char tipo, long numero, Proveedor proveedor) {
        return findByQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
                + "WHERE o.tipo='" + Character.toUpperCase(tipo) + "' AND o.numero = " + numero + " AND o.proveedor.id=" + proveedor.getId());
    }
}
