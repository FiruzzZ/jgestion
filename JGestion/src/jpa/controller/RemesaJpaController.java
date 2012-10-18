package jpa.controller;

import controller.DAO;
import controller.OperacionesBancariasController;
import controller.UsuarioController;
import entity.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.JGestionUtils;

/**
 *
 * @author FiruzzZ
 */
public class RemesaJpaController extends AbstractDAO<Remesa, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Remesa findByNumero(long numero) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Remesa> cq = cb.createQuery(getEntityClass());
        Root<Remesa> from = cq.from(getEntityClass());
        cq.select(from).
                where(cb.equal(from.get(Remesa_.numero), numero));
        return getEntityManager().createQuery(cq).getSingleResult();
    }

    public List<Remesa> findByFactura(FacturaCompra facturaCompra) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Remesa> cq = cb.createQuery(getEntityClass());
        Root<DetalleRemesa> from = cq.from(DetalleRemesa.class);
        cq.select(from.get(DetalleRemesa_.remesa)).
                where(cb.equal(from.get(DetalleRemesa_.facturaCompra), facturaCompra));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<DetalleRemesa> findDetalleRemesaByFactura(FacturaCompra factura) {
        return getEntityManager().
                createQuery("SELECT o FROM DetalleRemesa o"
                + " WHERE o.facturaCompra = :facturaCompra", DetalleRemesa.class).
                setParameter("facturaCompra", factura).getResultList();
    }

    @Override
    public void create(Remesa remesa) {
        entityManager = getEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        entityManager.persist(remesa);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        List<Object> pagosPost = new ArrayList<Object>(remesa.getPagosEntities().size());
        for (Object object : remesa.getPagosEntities()) {
            if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                pago.setComprobanteEgreso(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(remesa, true));
                entityManager.persist(pago);
                CuentabancariaMovimientos cbm = new CuentabancariaMovimientos(pago.getFechaCheque(), pago.getComprobanteEgreso(), null, BigDecimal.ZERO, pago.getImporte(), false, UsuarioController.getCurrentUser(), new OperacionesBancariasController().getOperacion(OperacionesBancariasController.EXTRACCION), pago.getCuentabancaria(), null, pago);
                entityManager.persist(cbm);
                pagosPost.add(pago);
            } else if (object instanceof ChequeTerceros) {
                ChequeTerceros pago = (ChequeTerceros) object;
                pago.setComprobanteEgreso(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(remesa, true));
                entityManager.merge(pago);
                pagosPost.add(pago);
            } else if (object instanceof NotaCreditoProveedor) {
                NotaCreditoProveedor pago = (NotaCreditoProveedor) object;
                pago.setDesacreditado(pago.getImporte());
                pago.setRemesa(remesa);
                entityManager.merge(object);
                pagosPost.add(pago);
            } else if (object instanceof ComprobanteRetencion) {
                ComprobanteRetencion pago = (ComprobanteRetencion) object;
                entityManager.persist(pago);
                pagosPost.add(pago);
            } else if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                CajaMovimientos cm = new CajaMovimientosJpaController().findCajaMovimientoAbierta(remesa.getCaja());
                pago.setCajaMovimientos(cm);
                pago.setDescripcion(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(remesa, true));
                pago.setNumero(Long.valueOf(remesa.getId()));
                entityManager.persist(pago);
                pagosPost.add(pago);
            }
        }
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        for (Object object : pagosPost) {
            Integer tipo = null, id = null;
            if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                tipo = 0;
                id = pago.getId();
            } else if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                tipo = 1;
                id = pago.getId();
            } else if (object instanceof ChequeTerceros) {
                ChequeTerceros pago = (ChequeTerceros) object;
                tipo = 2;
                id = pago.getId();
            } else if (object instanceof NotaCredito) {
                NotaCredito pago = (NotaCredito) object;
                tipo = 3;
                id = pago.getId();
            } else if (object instanceof ComprobanteRetencion) {
                ComprobanteRetencion pago = (ComprobanteRetencion) object;
                tipo = 4;
                id = pago.getId();
            }
            RemesaPagos rp = new RemesaPagos(null, tipo, id, remesa);
            entityManager.persist(rp);
        }
        entityManager.getTransaction().commit();
    }

    public Integer getNextNumero(Sucursal sucursal) {
        Integer next = 1; //sucursal.getRemesa();
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

    public Remesa find(Sucursal sucursal, Integer numero) {
        try {
            return getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
                    + " WHERE o.sucursal.id=" + sucursal.getId() + " AND o.numero=" + numero, getEntityClass()).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
