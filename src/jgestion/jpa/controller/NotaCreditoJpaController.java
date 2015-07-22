package jgestion.jpa.controller;

import jgestion.entity.DetalleNotaCredito;
import jgestion.entity.NotaCredito;
import jgestion.entity.Sucursal;
import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 *
 * @author FiruzzZ
 */
public class NotaCreditoJpaController extends JGestionJpaImpl<NotaCredito, Integer> {

    public NotaCreditoJpaController() {
    }

    @Override
    public void persist(NotaCredito notaCredito) {
        Collection<DetalleNotaCredito> toAttach = notaCredito.getDetalle();
        try {
            for (DetalleNotaCredito detalleNotaCredito : toAttach) {
                detalleNotaCredito.setNotaCredito(notaCredito);
            }
            super.persist(notaCredito);
        } finally {
            closeEntityManager();
        }
    }

    public Integer getNextNumero(Sucursal sucursal, char tipo) {
        EntityManager em = getEntityManager();
        Integer next;
        if (Character.toUpperCase(tipo) == 'A') {
            next = sucursal.getNotaCredito_a();
        } else if (Character.toUpperCase(tipo) == 'B') {
            next = sucursal.getNotaCredito_b();
        } else if (Character.toUpperCase(tipo) == 'C') {
            next = sucursal.getNotaCredito_c();
        } else {
            throw new IllegalArgumentException("Parameter tipo not valid, no corresponde a ningún tipo de Nota crédito.");
        }
        Object o = em.createQuery("SELECT MAX(o.numero) FROM " + getAlias()
                + " WHERE o.tipo ='" + Character.toUpperCase(tipo) + "'"
                + " AND o.sucursal.id= " + sucursal.getId()).getSingleResult();
        if (o != null) {
            Integer nextNumeroSegunDB = 1 + Integer.valueOf(o.toString());
            if (nextNumeroSegunDB > next) {
                //quiere decir que hay registrado un comprobante con mayor numeracion que supera la configuración de la sucursal
                next = nextNumeroSegunDB;
            }
        }
        return next;
    }

    public NotaCredito findBy(Sucursal sucursal, char tipo, int numero) {
        try {
            return findByQuery(getSelectFrom()
                    + " WHERE o.sucursal.id=" + sucursal.getId()
                    + " AND o.numero=" + numero
                    + " AND o.tipo='" + Character.toUpperCase(tipo) + "'");
        } catch (NoResultException e) {
            return null;
        }
    }
}
