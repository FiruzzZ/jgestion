package jpa.controller;

import controller.DAO;
import entity.DetalleVenta;
import entity.FacturaVenta;
import entity.FacturaVenta_;
import entity.Sucursal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
public class FacturaVentaJpaController extends AbstractDAO<FacturaVenta, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    @Override
    public FacturaVenta find(Integer id) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<FacturaVenta> query = cb.createQuery(getEntityClass());
        Root<FacturaVenta> from = query.from(getEntityClass());
        query.where(cb.equal(from.get(FacturaVenta_.id), id));
        return getEntityManager().createQuery(query).setHint(QueryHints.REFRESH, Boolean.TRUE).getSingleResult();
    }

    public Integer getNextNumeroFactura(Sucursal sucursal, char tipo) {
        EntityManager em = getEntityManager();
        Integer next = null;
        if (Character.toUpperCase(tipo) == 'A') {
            next = sucursal.getFactura_a();
        } else if (Character.toUpperCase(tipo) == 'B') {
            next = sucursal.getFactura_b();
        } else {
            throw new IllegalArgumentException("Parameter tipo not valid, no corresponde a ningún tipo de Factura venta.");
        }
        Object o = em.createQuery("SELECT MAX(o.numero) FROM FacturaVenta o"
                + " WHERE o.tipo ='" + Character.toUpperCase(tipo) + "'"
                + " AND o.sucursal.id= " + sucursal.getId()).getSingleResult();
        if (o != null) {
            Integer nextNumeroSegunDB = 1 + Integer.valueOf(o.toString());
            if (nextNumeroSegunDB > next) {
                //quiere decir que la numeración ya supera la configuración
                next = nextNumeroSegunDB;
            }
        }
        return next;
    }

    public Integer getNextMovimientoInterno() {
        EntityManager em = getEntityManager();
        try {
            Object o = em.createQuery("SELECT MAX(o.movimientoInterno) FROM FacturaVenta o").getSingleResult();
            if (o == null) {
                return 1;
            } else {
                return 1 + Integer.valueOf(o.toString());
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public FacturaVenta find(char tipo, Sucursal sucursal, Integer numero) {
        try {
            return getEntityManager().createQuery("SELECT o FROM FacturaVenta o"
                    + " WHERE o.sucursal.id=" + sucursal.getId()
                    + " AND o.numero=" + numero
                    + " AND o.tipo='" + Character.toUpperCase(tipo) + "'", FacturaVenta.class).
                    getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
