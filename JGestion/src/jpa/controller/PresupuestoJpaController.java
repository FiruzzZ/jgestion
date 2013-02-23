package jpa.controller;

import controller.DAO;
import entity.Presupuesto;
import entity.Sucursal;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class PresupuestoJpaController extends AbstractDAO<Presupuesto, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Integer getNextNumero(Sucursal sucursal) {
        try {
            return (Integer) getEntityManager().createQuery("SELECT MAX(o.numero)"
                    + " FROM " + getEntityClass().getSimpleName() + " o"
                    + " WHERE o.sucursal.id = " + sucursal.getId()).getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Pintó el 1er de " + getEntityClass().getSimpleName() + ", Sucursa=" + sucursal.getNombre() + "(" + sucursal.getPuntoVenta() + ")");
            return 1;
        }
    }
}
