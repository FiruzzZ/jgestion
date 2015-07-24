package jgestion.jpa.controller;

import jgestion.controller.DAO;
import jgestion.entity.Remito;
import jgestion.entity.Sucursal;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author Administrador
 */
public class RemitoJpaController extends AbstractDAO<Remito, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Integer getNextNumero(Sucursal sucursal) {
        Integer next = sucursal.getRemito();
        Object l = getEntityManager().createQuery("SELECT MAX(o.numero)"
                + " FROM " + getEntityClass().getSimpleName() + " o"
                + " WHERE o.sucursal.id = " + sucursal.getId()).getSingleResult();
        if (l != null) {
            Integer nextNumeroSegunDB = 1 + Integer.valueOf(l.toString());
            if (nextNumeroSegunDB > next) {
                //quiere decir que la numeración ya supera la configuración
                next = nextNumeroSegunDB;
            }
        }
        return next;
    }

    public Remito findBy(Sucursal sucursal, Integer numero) {
        try {
            return findByQuery(getSelectFrom()
                    + " WHERE o.sucursal.id=" + sucursal.getId()
                    + " AND o.numero=" + numero
            );
        } catch (NoResultException e) {
            return null;
        }

    }
}
