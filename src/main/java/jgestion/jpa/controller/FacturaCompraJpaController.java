package jgestion.jpa.controller;

import jgestion.controller.DetalleCompraJpaController;
import jgestion.entity.DetalleCompra;
import jgestion.entity.FacturaCompra;
import jgestion.entity.FacturaCompra_;
import jgestion.entity.Sucursal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.entity.Proveedor;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class FacturaCompraJpaController extends JGestionJpaImpl<FacturaCompra, Integer> {

    public FacturaCompraJpaController() {
    }

    @Override
    public void persist(FacturaCompra o) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<DetalleCompra> detallesCompraListToPersist = o.getDetalleCompraList();
            o.setDetalleCompraList(new ArrayList<>());
            em.persist(o);
            em.getTransaction().commit();
            DetalleCompraJpaController dcController = new DetalleCompraJpaController();
            for (DetalleCompra detallesCompra : detallesCompraListToPersist) {
                detallesCompra.setFactura(o);
                dcController.create(detallesCompra);
                o.getDetalleCompraList().add(detallesCompra);
            }
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalArgumentException(ex.getMessage());
        } finally {
            if (em != null) {
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }

    public long getMaxNumeroComprobante(Sucursal sucursal, Character tipo) {
        getEntityManager();
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<FacturaCompra> from = cq.from(getEntityClass());
            cq.where(cb.equal(from.get(FacturaCompra_.tipo), tipo),
                    cb.equal(from.get(FacturaCompra_.sucursal), sucursal));
            cq.select(cb.max(from.get(FacturaCompra_.numero)));
            Long max = getEntityManager().createQuery(cq).getSingleResult();
            if (max == null) {
                return 0;
            } else {
                String s = UTIL.AGREGAR_CEROS(max, 12);
                return Long.valueOf(s.substring(4));
            }
        } finally {
            getEntityManager().close();
        }
    }

    public FacturaCompra findBy(Proveedor proveedor, String tipo, long numero, boolean anulada) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<FacturaCompra> cq = cb.createQuery(getEntityClass());
        Root<FacturaCompra> from = cq.from(getEntityClass());
        cq.where(cb.equal(from.get(FacturaCompra_.tipo), tipo),
                cb.equal(from.get(FacturaCompra_.proveedor), proveedor),
                cb.equal(from.get(FacturaCompra_.numero), numero),
                cb.equal(from.get(FacturaCompra_.anulada), anulada)
        );
        try {
            return getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
