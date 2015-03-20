package jgestion.jpa.controller;

import jgestion.entity.*;
import jgestion.controller.CtacteProveedorController;
import jgestion.controller.DAO;
import jgestion.controller.DetalleCajaMovimientosController;
import jgestion.controller.Valores;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.enums.ChequeEstado;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
    public void persist(Remesa remesa) {
        entityManager = getEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        entityManager.persist(remesa);
        for (DetalleRemesa d : remesa.getDetalle()) {
            if (d.getNotaDebitoProveedor() != null) {
                d.getNotaDebitoProveedor().setRemesa(remesa);
                entityManager.merge(d.getNotaDebitoProveedor());
            }
        }
        entityManager.getTransaction().commit();
        boolean todoBien = false;
        List<Object> pagosPost = new ArrayList<>(remesa.getPagosEntities().size());
        try {
            entityManager.getTransaction().begin();
            for (Object object : remesa.getPagosEntities()) {
                if (object instanceof ChequePropio) {
                    ChequePropio pago = (ChequePropio) object;
                    pago.setComprobanteEgreso(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(remesa, true));
                    entityManager.persist(pago);
                    pagosPost.add(pago);
                } else if (object instanceof ChequeTerceros) {
                    ChequeTerceros pago = (ChequeTerceros) object;
                    pago.setEstado(ChequeEstado.ENDOSADO.getId());
                    pago.setFechaEndoso(remesa.getFechaRemesa());
                    Proveedor proveedor;
                    if (!remesa.getDetalle().isEmpty()) {
                        if (remesa.getDetalle().get(0).getFacturaCompra() != null) {
                            proveedor = remesa.getDetalle().get(0).getFacturaCompra().getProveedor();
                        } else {
                            proveedor = remesa.getDetalle().get(0).getNotaDebitoProveedor().getProveedor();
                        }
                    } else {
                        //cuando es "A conciliar", no tiene detalle de donde sacar el proveedor
                        proveedor = remesa.getProveedor();
                    }
                    pago.setEndosatario(proveedor.getNombre());
                    pago.setComprobanteEgreso(getEntityClass().getSimpleName() + " " + JGestionUtils.getNumeracion(remesa, true));
                    entityManager.merge(pago);
                    pagosPost.add(pago);
                } else if (object instanceof NotaCreditoProveedor) {
                    NotaCreditoProveedor pago = (NotaCreditoProveedor) object;
                    pago.setDesacreditado(pago.getImporte());
                    pago.setRemesa(remesa);
                    entityManager.merge(pago);
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
                } else if (object instanceof CuentabancariaMovimientos) {
                    CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                    entityManager.persist(pago);
                    pagosPost.add(pago);
                } else if (object instanceof Especie) {
                    Especie pago = (Especie) object;
                    entityManager.persist(pago);
                    pagosPost.add(pago);
                } else {
                    throw new IllegalArgumentException("Forma Pago Remesa no válida:" + object);
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
                } else if (object instanceof NotaCreditoProveedor) {
                    NotaCreditoProveedor pago = (NotaCreditoProveedor) object;
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
                RemesaPagos rp = new RemesaPagos(null, tipo, id, remesa);
                entityManager.persist(rp);
            }
            entityManager.getTransaction().commit();
            todoBien = true;
        } finally {
            if (!todoBien) {
                closeEntityManager();
                remove(remesa);
                for (Object object : pagosPost) {
                    if (object instanceof ChequeTerceros) {
                        ChequeTerceros pago = (ChequeTerceros) object;
                        pago.setEstado(ChequeEstado.CARTERA.getId());
                        pago.setFechaEndoso(null);
                        pago.setEndosatario(null);
                        pago.setComprobanteEgreso(null);
                        new ChequeTercerosJpaController().merge(pago);
                    } else if (object instanceof ChequePropio) {
                        ChequePropio pago = (ChequePropio) object;
                        new ChequePropioJpaController().remove(pago);
                    } else if (object instanceof DetalleCajaMovimientos) {
                        DetalleCajaMovimientos pago = (DetalleCajaMovimientos) object;
                        new DetalleCajaMovimientosController().remove(pago);
                    } else if (object instanceof NotaCreditoProveedor) {
                        NotaCreditoProveedor pago = (NotaCreditoProveedor) object;
                        pago.setDesacreditado(BigDecimal.ZERO);
                        pago.setRemesa(null);
                        new NotaCreditoProveedorJpaController().merge(pago);
                    } else if (object instanceof ComprobanteRetencion) {
                        ComprobanteRetencion pago = (ComprobanteRetencion) object;
                        new ComprobanteRetencionJpaController().remove(pago);
                    } else if (object instanceof CuentabancariaMovimientos) {
                        CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                        new CuentabancariaMovimientosJpaController().remove(pago);
                    } else if (object instanceof Especie) {
                        Especie pago = (Especie) object;
                        new EspecieJpaController().remove(pago);
                    }
                }
            }
        }
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

    public void anular(Remesa remesa) throws Exception {
        EntityManager em = getEntityManager();
        List<DetalleRemesa> detalleRemesaList = remesa.getDetalle();
        CtacteProveedor ctaCteProveedor;
        try {
            em.getTransaction().begin();
            for (DetalleRemesa dr : detalleRemesaList) {
                if (dr.getFacturaCompra() != null) {
                    //se resta la entrega ($) que implicaba este detalle con respecto a la factura
                    ctaCteProveedor = new CtacteProveedorController().findCtacteProveedorByFactura(dr.getFacturaCompra().getId());
                    ctaCteProveedor.setEntregado(ctaCteProveedor.getEntregado().subtract(dr.getMontoEntrega()));
                    // y si había sido pagada en su totalidad..
                    if (ctaCteProveedor.getEstado() == Valores.CtaCteEstado.PAGADA.getId()) {
                        ctaCteProveedor.setEstado(Valores.CtaCteEstado.PENDIENTE.getId());
                    }
                    em.merge(ctaCteProveedor);
                } else {
                    NotaDebitoProveedor nd = em.find(dr.getNotaDebitoProveedor().getClass(), dr.getNotaDebitoProveedor().getId());
                    nd.setRemesa(null);
                    em.merge(nd);
                }
            }
            for (Object object : remesa.getPagosEntities()) {
                if (object instanceof ChequePropio) {
                    ChequePropio pago = (ChequePropio) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    if (pago.getEstado() != ChequeEstado.CARTERA.getId()) {
    //                    CuentabancariaMovimientos cbm = new CuentabancariaMovimientosJpaController().findBy(pago);
    //                    cbm = em.find(cbm.getClass(), cbm.getId());
    //                    em.remove(cbm);
                        throw new MessageException("¡ANULACIÓN CANCELADA!:"
                                + "\nEl Cheque Propio " + pago.getBanco().getNombre() + " " + pago.getNumero() 
                                + ", Importe $" + pago.getImporte()
                                + "\nfue COBRADO/DEBITADO, no se encuentra mas en " + ChequeEstado.CARTERA);
                    }
                    em.remove(pago);
                } else if (object instanceof ChequeTerceros) {
                    ChequeTerceros pago = (ChequeTerceros) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    pago.setComprobanteEgreso(null);
                    pago.setEstado(ChequeEstado.CARTERA.getId());
                    em.merge(pago);
                } else if (object instanceof NotaCreditoProveedor) {
                    NotaCreditoProveedor pago = (NotaCreditoProveedor) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    pago.setRemesa(null);
                    em.merge(pago);
                } else if (object instanceof ComprobanteRetencion) {
                    ComprobanteRetencion pago = (ComprobanteRetencion) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    em.remove(pago);
                } else if (object instanceof DetalleCajaMovimientos) {
//                    DetalleCajaMovimientos dcm = (DetalleCajaMovimientos) object;
//                    rp = em.find(RemesaPagos.class, dcm.getId());
                    new CajaMovimientosJpaController().anular(remesa);
                } else if (object instanceof CuentabancariaMovimientos) {
                    CuentabancariaMovimientos pago = (CuentabancariaMovimientos) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    em.remove(pago);
                } else if (object instanceof Especie) {
                    Especie pago = (Especie) object;
                    pago = em.find(pago.getClass(), pago.getId());
                    em.remove(pago);
                }
            }
            remesa.setEstado(false);
            em.getTransaction().commit();
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

    public void conciliar(Remesa remesa) {
        getEntityManager();
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        Remesa old = find(remesa.getSucursal(), remesa.getNumero());
        old.setDetalle(remesa.getDetalle());
        old.setPorConciliar(false);
        old.setMontoEntrega(remesa.getMonto());
        for (DetalleRemesa d : old.getDetalle()) {
            d.setRemesa(old);
            entityManager.persist(d);
            if (d.getNotaDebitoProveedor() != null) {
                d.getNotaDebitoProveedor().setRemesa(old);
                entityManager.merge(d.getNotaDebitoProveedor());
            }
        }
        entityManager.merge(old);
        entityManager.getTransaction().commit();
        remesa.setId(old.getId());
        remesa.setDetalle(old.getDetalle());
        closeEntityManager();
    }
}
