package jgestion.jpa.controller;

import jgestion.entity.DetallePresupuesto;
import jgestion.entity.Presupuesto;
import jgestion.entity.Sucursal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class PresupuestoJpaController extends JGestionJpaImpl<Presupuesto, Integer> {

    public PresupuestoJpaController() {
    }


    @Override
    public void persist(Presupuesto presupuesto) {
        Integer nextNumero = getNextNumero(presupuesto.getSucursal());
        presupuesto.setNumero(nextNumero);
        List<DetallePresupuesto> toAttach = presupuesto.getDetallePresupuestoList();
        presupuesto.setDetallePresupuestoList(new ArrayList<DetallePresupuesto>());
        getEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(presupuesto);
            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();
            for (DetallePresupuesto detalle : toAttach) {
                detalle.setPresupuesto(presupuesto);
                entityManager.persist(detalle);
            }
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        } finally {
            closeEntityManager();
        }
    }

    public Integer getNextNumero(Sucursal sucursal) {
        try {
            Integer next = (Integer) getEntityManager().createQuery("SELECT MAX(o.numero)"
                    + " FROM " + getEntityClass().getSimpleName() + " o"
                    + " WHERE o.sucursal.id = " + sucursal.getId()).getSingleResult();
            if (next == null) {
                next = 1;
            }
            return next +1;
        } catch (NoResultException e) {
            System.out.println("Pintó el 1er de " + getEntityClass().getSimpleName() + ", Sucursa=" + sucursal.getNombre() + "(" + sucursal.getPuntoVenta() + ")");
            return 1;
        }
    }
}
