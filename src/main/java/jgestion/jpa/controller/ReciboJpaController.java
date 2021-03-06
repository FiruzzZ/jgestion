package jgestion.jpa.controller;

import java.lang.reflect.Method;
import jgestion.entity.ComprobanteRetencion;
import jgestion.entity.ReciboPagos;
import jgestion.entity.ChequeTerceros;
import jgestion.entity.NotaDebito;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.entity.Sucursal;
import jgestion.entity.FacturaVenta;
import jgestion.entity.NotaCredito;
import jgestion.entity.ChequePropio;
import jgestion.entity.CajaMovimientos;
import jgestion.entity.DetalleRecibo;
import jgestion.entity.Recibo;
import jgestion.entity.Especie;
import jgestion.entity.CuentabancariaMovimientos;
import jgestion.entity.CtacteCliente;
import jgestion.controller.CtacteClienteController;
import jgestion.controller.Valores;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.enums.ChequeEstado;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.DAOException;
import jgestion.entity.DetalleRecibo_;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author FiruzzZ
 */
public class ReciboJpaController extends JGestionJpaImpl<Recibo, Integer> {

    public ReciboJpaController() {
    }

    public Integer getNextNumero(Sucursal sucursal, String tipo) {
        return getNextNumero(sucursal, tipo.charAt(0));
    }

    public Integer getNextNumero(Sucursal sucursal, char tipo) {
        EntityManager em = getEntityManager();
        Integer next;
        switch (Character.toUpperCase(tipo)) {
            case 'A':
                next = sucursal.getRecibo_a();
                break;
            case 'B':
                next = sucursal.getRecibo_b();
                break;
            case 'C':
                next = sucursal.getRecibo_c();
                break;
            case 'X':
                next = sucursal.getRecibo_x();
                break;
            default:
                throw new IllegalArgumentException("Parameter tipo not valid, no corresponde a ningún tipo de Recibo.");
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

    /**
     * La anulación de una Recibo, resta a <code>CtaCteCliente.entregado</code> los pagos/entregas
     * (parciales/totales) realizados de cada DetalleRecibo y cambia
     * <code>Recibo.estado = false</code>
     *
     * @param recibo
     * @throws MessageException
     * @throws Exception si Recibo es null, o si ya está anulado
     */
    public void anular(Recibo recibo) throws MessageException, Exception {
        if (recibo == null) {
            throw new MessageException(getEntityClass().getSimpleName() + " no válido");
        }
        if (!recibo.getEstado()) {
            throw new MessageException("El " + getEntityClass().getSimpleName() + " ya está anulado");
        }

        EntityManager em = getEntityManager();
        List<DetalleRecibo> detalleReciboList = recibo.getDetalle();
        CtacteCliente ctaCteCliente;
        try {
            em.getTransaction().begin();
            for (DetalleRecibo dr : detalleReciboList) {
                if (dr.getFacturaVenta() != null) {
                    //se resta la entrega ($) que implicaba este detalle con respecto a CADA factura
                    ctaCteCliente = new CtacteClienteController().findBy(dr.getFacturaVenta());
                } else {
                    ctaCteCliente = new CtacteClienteJpaController().findByNotaDebito(dr.getNotaDebito());
                }
                ctaCteCliente.setEntregado(ctaCteCliente.getEntregado().subtract(dr.getMontoEntrega()));
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
                    pago.setEstado(ChequeEstado.ENDOSADO.getId());
                    em.merge(pago);
                } else if (object instanceof ChequeTerceros) {
                    ChequeTerceros pago = (ChequeTerceros) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    pago.setComprobanteIngreso(null);
                    em.merge(pago);
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
    public List<DetalleRecibo> findDetalleReciboBy(FacturaVenta factura) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<DetalleRecibo> query = cb.createQuery(DetalleRecibo.class);
        Root<DetalleRecibo> from = query.from(query.getResultType());
        query.where(cb.equal(from.get(DetalleRecibo_.facturaVenta), factura));
        return getEntityManager().createQuery(query).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<DetalleRecibo> findDetalleReciboBy(NotaDebito notaDebito) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<DetalleRecibo> query = cb.createQuery(DetalleRecibo.class);
        Root<DetalleRecibo> from = query.from(query.getResultType());
        query.where(cb.equal(from.get(DetalleRecibo_.notaDebito), notaDebito));
        return getEntityManager().createQuery(query).getResultList();
    }

    @Override
    public void persist(Recibo recibo) {
        getEntityManager().close();
        entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(recibo);
        entityManager.getTransaction().commit();
        List<Object> pagosPost = null;
        try {
            entityManager.getTransaction().begin();
            pagosPost = new ArrayList<>(recibo.getPagosEntities().size());
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
                    pago.setComprobanteIngreso(JGestionUtils.getNumeracion(recibo, true));
                    entityManager.merge(pago);
                    pagosPost.add(pago);
                } else if (object instanceof ChequeTerceros) {
                    ChequeTerceros pago = (ChequeTerceros) object;
                    pago.setComprobanteIngreso(JGestionUtils.getNumeracion(recibo, true));
                    if (pago.getId() == null) {
                        entityManager.persist(pago);
                    } else {
                        entityManager.merge(pago);
                    }
                    pagosPost.add(pago);
                } else if (object instanceof NotaCredito) {
                    NotaCredito pago = (NotaCredito) object;
                    pago.setRecibo(recibo);
                    entityManager.merge(object);
                    pagosPost.add(pago);
                } else if (object instanceof ComprobanteRetencion) {
                    ComprobanteRetencion pago = (ComprobanteRetencion) object;
                    entityManager.persist(pago);
                    pagosPost.add(pago);
                } else if (object instanceof CuentabancariaMovimientos) {
                    CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                    pago.setOperacionesBancarias(entityManager.find(pago.getOperacionesBancarias().getClass(), pago.getOperacionesBancarias().getId()));
                    entityManager.persist(pago);
                    pagosPost.add(pago);
                } else if (object instanceof Especie) {
                    Especie pago = (Especie) object;
                    entityManager.persist(pago);
                    pagosPost.add(pago);
                }
            }
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            LogManager.getLogger().fatal("Error persistiendo Recibo.id=" + recibo.getId());
            entityManager = getEntityManager();
            if (!entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().begin();
            }
            for (Object object : pagosPost) {
                if (object instanceof DetalleCajaMovimientos
                        || object instanceof CuentabancariaMovimientos
                        || object instanceof Especie
                        || object instanceof ComprobanteRetencion) {
                    Object id = null;
                    try {
                        Method m = object.getClass().getDeclaredMethod("getId");
                        id = m.invoke(object);
                    } catch (Exception exx) {
                        throw new RuntimeException(exx);
                    }
                    if (id != null) {
                        Object toDelete = entityManager.find(object.getClass(), id);
                        entityManager.remove(toDelete);
                    }
                } else if (object instanceof NotaCredito) {
                    NotaCredito pago = (NotaCredito) object;
                    pago.setRecibo(null);
                    entityManager.merge(object);
                } else if (object instanceof ChequeTerceros) {
                    ChequeTerceros pago = (ChequeTerceros) object;
                    pago.setComprobanteIngreso(null);
                    entityManager.merge(object);
                }
            }
            entityManager.getTransaction().commit();
            remove(recibo);
            throw new DAOException("Error persistiendo Recibo.id=" + recibo.getId()
                    + "\n" + ex.getMessage(), ex);
        }
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
            } else if (object instanceof Especie) {
                Especie pago = (Especie) object;
                tipo = 6;
                id = pago.getId();
            }
            ReciboPagos rp = new ReciboPagos(null, tipo, id, recibo);
            entityManager.persist(rp);
        }
        entityManager.getTransaction().commit();
    }
}
