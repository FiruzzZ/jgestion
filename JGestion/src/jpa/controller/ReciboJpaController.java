package jpa.controller;

import controller.CtacteClienteController;
import controller.DAO;
import controller.Valores;
import controller.exceptions.MessageException;
import entity.*;
import entity.enums.ChequeEstado;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import jgestion.JGestionUtils;

/**
 *
 * @author FiruzzZ
 */
public class ReciboJpaController extends AbstractDAO<Recibo, Integer> {

    private EntityManager entityManager;

    public ReciboJpaController() {
        getEntityManager();
    }

    @Override
    protected final EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public Integer getNextNumero(Sucursal sucursal) {
        Integer next = sucursal.getRecibo();
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

    /**
     * La anulación de una Recibo, resta a
     * <code>CtaCteCliente.entregado</code> los pagos/entregas
     * (parciales/totales) realizados de cada DetalleRecibo y cambia
     * <code>Recibo.estado = false<code>
     *
     * @param recibo
     * @throws MessageException
     * @throws Exception si Recibo es null, o si ya está anulado
     */
    public void anular(Recibo recibo) throws MessageException, Exception {
        EntityManager em = getEntityManager();
        if (recibo == null) {
            throw new MessageException(getEntityClass().getSimpleName() + " no válido");
        }
        if (!recibo.getEstado()) {
            throw new MessageException("El " + getEntityClass().getSimpleName() + " ya está anulado");
        }

        List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
        CtacteCliente ctaCteCliente;
        try {
            em.getTransaction().begin();
            for (DetalleRecibo dr : detalleReciboList) {
                //se resta la entrega ($) que implicaba este detalle con respecto a CADA factura
                ctaCteCliente = new CtacteClienteController().findByFactura(dr.getFacturaVenta().getId());
                ctaCteCliente.setEntregado(ctaCteCliente.getEntregado() - dr.getMontoEntrega().doubleValue());
                // y si había sido pagada en su totalidad..
                if (ctaCteCliente.getEstado() == Valores.CtaCteEstado.PAGADA.getId()) {
                    ctaCteCliente.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
                }
                em.merge(ctaCteCliente);
            }
            for (Object object : recibo.getPagosEntities()) {
                if (object instanceof ChequePropio) {
                    ChequePropio pago = (ChequePropio) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    pago.setComprobanteIngreso(null);
//                    pago.setEstado(ChequeEstado.CARTERA.getId());
                    em.remove(pago);
                } else if (object instanceof ChequeTerceros) {
                    ChequeTerceros pago = (ChequeTerceros) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    if (pago.getEstado() != ChequeEstado.CARTERA.getId()) {
                        throw new MessageException("CANCELACIÓN DE ANULACIÓN:"
                                + "\nEl Cheque Tercero " + pago.getBanco().getNombre() + " " + pago.getNumero() + ", Importe $" + pago.getImporte()
                                + "\nfue utilizado, no se encuentra mas en \"Cartera\".");
                    }
                    em.remove(pago);
                } else if (object instanceof NotaCredito) {
                    NotaCredito pago = (NotaCredito) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    pago.setRecibo(null);
                    em.merge(pago);
                } else if (object instanceof ComprobanteRetencion) {
                    ComprobanteRetencion pago = (ComprobanteRetencion) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    em.remove(pago);
                } else if (object instanceof DetalleCajaMovimientos) {
                    new CajaMovimientosJpaController().anular(recibo);
                } else if (object instanceof CuentabancariaMovimientos) {
                    CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    em.remove(pago);
                }
            }
            for (int i = 0; i < recibo.getPagos().size(); i++) {
                recibo.getPagos().remove(i);
            }
            recibo.setEstado(false);
            em.getTransaction().commit();
            merge(recibo);
        } catch (MessageException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public Recibo find(Sucursal sucursal, Integer numero) {
        try {
            return getEntityManager().createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o "
                    + "WHERE o.sucursal.id=" + sucursal.getId() + " AND o.numero=" + numero, getEntityClass()).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<DetalleRecibo> findDetalleReciboEntitiesByFactura(FacturaVenta factura) {
        return getEntityManager().
                createNamedQuery("DetalleRecibo.findByFacturaVenta").
                setParameter("facturaVenta", factura).getResultList();
    }

    @Override
    public void create(Recibo recibo) {
        entityManager = getEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        entityManager.persist(recibo);
        for (DetalleRecibo d : recibo.getDetalleReciboList()) {
            if(d.getNotaDebito() != null) {
                d.setRecibo(recibo);
                entityManager.merge(d.getNotaDebito());
            }
        }
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        List<Object> pagosPost = new ArrayList<Object>(recibo.getPagosEntities().size());
        for (Object object : recibo.getPagosEntities()) {
            if (object instanceof DetalleCajaMovimientos) {
                DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                CajaMovimientos cm = new CajaMovimientosJpaController().findCajaMovimientoAbierta(recibo.getCaja());
                pago.setCajaMovimientos(cm);
                pago.setDescripcion(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(recibo, true));
                pago.setNumero(Long.valueOf(recibo.getId()));
                entityManager.persist(pago);
                pagosPost.add(pago);
            } else if (object instanceof ChequePropio) {
                ChequePropio pago = (ChequePropio) object;
                pago.setComprobanteIngreso(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(recibo, true));
                entityManager.merge(pago);
                pagosPost.add(pago);
            } else if (object instanceof ChequeTerceros) {
                ChequeTerceros pago = (ChequeTerceros) object;
                pago.setComprobanteIngreso(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(recibo, true));
                entityManager.persist(pago);
                pagosPost.add(pago);
            } else if (object instanceof NotaCredito) {
                NotaCredito pago = (NotaCredito) object;
                pago.setDesacreditado(pago.getImporte());
                pago.setRecibo(recibo);
                entityManager.merge(object);
                pagosPost.add(pago);
            } else if (object instanceof ComprobanteRetencion) {
                ComprobanteRetencion pago = (ComprobanteRetencion) object;
                entityManager.persist(pago);
                pagosPost.add(pago);
            } else if (object instanceof CuentabancariaMovimientos) {
                CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
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
            } else if (object instanceof CuentabancariaMovimientos) {
                CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                tipo = 5;
                id = pago.getId();
            }
            ReciboPagos rp = new ReciboPagos(null, tipo, id, recibo);
            entityManager.persist(rp);
        }
        entityManager.getTransaction().commit();
    }
}
