package jpa.controller;

import controller.DAO;
import entity.DetalleNotaCredito;
import entity.NotaCredito;
import entity.Sucursal;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class NotaCreditoJpaController extends AbstractDAO<NotaCredito, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public void persist(NotaCredito notaCredito) {
        Collection<DetalleNotaCredito> toAttach = notaCredito.getDetalleNotaCreditoCollection();
        notaCredito.setDetalleNotaCreditoCollection(new ArrayList<DetalleNotaCredito>());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(notaCredito);
            for (DetalleNotaCredito detalleNotaCredito : toAttach) {
                detalleNotaCredito.setNotaCredito(notaCredito);
                em.persist(detalleNotaCredito);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally {
            closeEntityManager();
        }
    }

    public Integer getNextNumero(Sucursal sucursal) {
        Integer next = sucursal.getNotaCredito();
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
}
