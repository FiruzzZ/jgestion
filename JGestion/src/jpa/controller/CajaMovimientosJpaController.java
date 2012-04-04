package jpa.controller;

import controller.*;
import controller.exceptions.MessageException;
import entity.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class CajaMovimientosJpaController extends AbstractDAO<CajaMovimientos, Integer> {

    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = DAO.getEntityManager();
        }
        return entityManager;
    }

    public void asentarMovimiento(FacturaCompra facturaCompra) throws Exception {
        Logger.getLogger(CajaMovimientosController.class).trace("asentarMovimiento (FacturaCompra): " + facturaCompra.getId() + " " + facturaCompra.getNumero());
        CajaMovimientos cm = findCajaMovimientoAbierta(facturaCompra.getCaja());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
            newDetalleCajaMovimiento.setIngreso(false);
            //el importe de las FacturaCompra son siempre negativos..
            newDetalleCajaMovimiento.setMonto(-facturaCompra.getImporte());
            newDetalleCajaMovimiento.setNumero(facturaCompra.getId());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.FACTU_COMPRA);
            newDetalleCajaMovimiento.setDescripcion("F" + facturaCompra.getTipo() + UTIL.AGREGAR_CEROS(facturaCompra.getNumero(), 12));
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void asentarMovimiento(FacturaVenta facturaVenta) throws Exception {
        Logger.getLogger(CajaMovimientosController.class).trace("asentarMovimiento FactuaVenta");
        //caja en la q se va asentar
        CajaMovimientos cm = findCajaMovimientoAbierta(facturaVenta.getCaja());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
            newDetalleCajaMovimiento.setIngreso(true);
            if (facturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CONTADO)) {
                newDetalleCajaMovimiento.setMonto(facturaVenta.getImporte());
            } else if (facturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CONTADO_CHEQUE)) {
                BigDecimal importe = new BigDecimal(facturaVenta.getImporte());
                ChequeTerceros chequeTerceros = new ChequeTercerosJpaController().findChequeTerceros(facturaVenta);
                BigDecimal noEfectivo = chequeTerceros.getImporte();
                //obtain the incom e cash from the sale 
                newDetalleCajaMovimiento.setMonto(noEfectivo.subtract(importe).doubleValue());
            } else {
                throw new IllegalArgumentException(facturaVenta.getClass() + ".id=" + facturaVenta.getId()
                        + " contiene FormaPago=" + facturaVenta.getFormaPagoEnum() + " NO ASENTABLE COMO MOVIMIENTO CONTABLE.");
            }
            newDetalleCajaMovimiento.setNumero(facturaVenta.getId());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.FACTU_VENTA);
            newDetalleCajaMovimiento.setDescripcion(getDescripcion(facturaVenta));
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void asentarMovimiento(Recibo recibo) throws Exception {
        //caja en la q se va asentar
        CajaMovimientos cm = findCajaMovimientoAbierta(recibo.getCaja());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
            newDetalleCajaMovimiento.setIngreso(true);
            newDetalleCajaMovimiento.setMonto(recibo.getMonto());
            newDetalleCajaMovimiento.setNumero(recibo.getId());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.RECIBO);
            newDetalleCajaMovimiento.setDescripcion("R" + UTIL.AGREGAR_CEROS(recibo.getId(), 12));
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void asentarMovimiento(Remesa remesa) throws Exception {
        System.out.println("asentarMovimiento (Remesa)");
        //caja en la q se va asentar
        CajaMovimientos cm = findCajaMovimientoAbierta(remesa.getCaja());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
            newDetalleCajaMovimiento.setIngreso(false);
            newDetalleCajaMovimiento.setMonto(-remesa.getMonto());
            newDetalleCajaMovimiento.setNumero(remesa.getId());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.REMESA);
            String descripcion = "R" + UTIL.AGREGAR_CEROS(remesa.getNumero(), 12);
            newDetalleCajaMovimiento.setDescripcion(descripcion);
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void asentarMovimiento(ChequeTerceros cheque, Caja caja) throws Exception {
        CajaMovimientos cm = findCajaMovimientoAbierta(caja);
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
            newDetalleCajaMovimiento.setIngreso(true);
            newDetalleCajaMovimiento.setMonto(cheque.getImporte().doubleValue());
            newDetalleCajaMovimiento.setNumero(cheque.getId());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.CHEQUE_TERCEROS);
            newDetalleCajaMovimiento.setDescripcion("CH" + cheque.getNumero() + " (" + cheque.getCliente().getNombre() + ")");
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void asentarMovimiento(ChequePropio cheque, Caja caja) throws Exception {
        CajaMovimientos cm = findCajaMovimientoAbierta(caja);
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
            newDetalleCajaMovimiento.setIngreso(false);
            newDetalleCajaMovimiento.setMonto(-cheque.getImporte().doubleValue());
            newDetalleCajaMovimiento.setNumero(cheque.getId());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.CHEQUE_PROPIO);
            newDetalleCajaMovimiento.setDescripcion("CH" + cheque.getNumero() + " (" + cheque.getProveedor().getNombre() + ")");
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void anular(Recibo recibo) throws MessageException, Exception {
        if (recibo.getEstado()) {
            throw new MessageException("No se puede anular el recibo Nº" + recibo.getId() + " porque ESTADO =" + recibo.getEstado());
        }
        //caja en la q se va asentar
        CajaMovimientos cm = findCajaMovimientoAbierta(recibo.getCaja());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
            newDetalleCajaMovimiento.setIngreso(false);
            newDetalleCajaMovimiento.setMonto(-recibo.getMonto());
            newDetalleCajaMovimiento.setNumero(recibo.getId());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.RECIBO);
            newDetalleCajaMovimiento.setDescripcion("R" + UTIL.AGREGAR_CEROS(recibo.getId(), 12) + " [ANULADO]");
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Realiza todos los procesos de anulación de una FacturaVenta/Comprobante.
     * <html><ul> <li> Asentar el reintegro del importe de facturaVenta a la
     * cajaMovimientoDestino. <li> Re-establece los stock de Productos
     * involucrados. <li> Si es por CtaCte, ccc.estado = 3 (anulado), así como
     * los posible pagos realizados. </ul></html>
     *
     * @param facturaVenta la cual se quiere anular
     * @param cajaMovimientoDestino en la cual se van a registrar los
     * movimientos contables
     * @throws Exception
     */
    public void anular(FacturaVenta facturaVenta, CajaMovimientos cajaMovimientoDestino) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            facturaVenta = em.find(FacturaVenta.class, facturaVenta.getId());
            cajaMovimientoDestino = em.find(CajaMovimientos.class, cajaMovimientoDestino.getId());
            em.lock(facturaVenta, LockModeType.PESSIMISTIC_WRITE);

            //generic info in case of anullation..
            DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
            newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoDestino);
            newDetalleCajaMovimiento.setIngreso(false);
            newDetalleCajaMovimiento.setNumero(facturaVenta.getId());
            newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
            newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.ANULACION);

            if (facturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CTA_CTE)) {
                newDetalleCajaMovimiento.setMonto(-facturaVenta.getImporte());
                newDetalleCajaMovimiento.setDescripcion(getDescripcion(facturaVenta) + " [ANULADA]");
                new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
            } else if (facturaVenta.getFormaPagoEnum().equals(Valores.FormaPago.CTA_CTE)) {
                CtacteCliente ccc = new CtacteClienteJpaController().findCtacteClienteByFactura(facturaVenta.getId());
                if (ccc.getEntregado() > 0) {
                    //find all receipts (Recibo's) that contains a payment of the bill (FacturaVenta)
                    List<Recibo> recibosList = new ReciboController().findRecibosByFactura(facturaVenta);
                    boolean detalleUnico;
                    for (Recibo reciboQueEnSuDetalleContieneLaFacturaVenta : recibosList) {
                        //if this is setted as TRUE, the entire Recibo must be annulled
                        detalleUnico = (reciboQueEnSuDetalleContieneLaFacturaVenta.getDetalleReciboList().size() == 1);
                        //NOTE!: A Recibo can have two details of the same facturaVenta.
                        //One with DetalleRecibo.acreditado = FALSE and one with TRUE
                        for (DetalleRecibo detalleRecibo : reciboQueEnSuDetalleContieneLaFacturaVenta.getDetalleReciboList()) {
                            if (detalleRecibo.getFacturaVenta().equals(facturaVenta)) {
                                detalleRecibo.setObservacion("[ANULADO] " + detalleRecibo.getObservacion());
                                detalleRecibo.setAnulado(true);
                                reciboQueEnSuDetalleContieneLaFacturaVenta.setMonto(
                                        reciboQueEnSuDetalleContieneLaFacturaVenta.getMonto() - detalleRecibo.getMontoEntrega());
                                if (detalleRecibo.isAcreditado()) {
                                    DetalleAcreditacion anular = new DetalleAcreditacionJpaController().anular(detalleRecibo);
                                    new NotaCreditoJpaController().acreditar(anular);
                                } else {
                                    newDetalleCajaMovimiento.setMonto(-detalleRecibo.getMontoEntrega());
                                    newDetalleCajaMovimiento.setDescripcion(getDescripcion(facturaVenta) + " -> R" + reciboQueEnSuDetalleContieneLaFacturaVenta.getId() + " [ANULADA]");
                                    new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
                                }
                                em.merge(detalleRecibo);
                                reciboQueEnSuDetalleContieneLaFacturaVenta.setEstado(!detalleUnico);
                                em.merge(reciboQueEnSuDetalleContieneLaFacturaVenta);
                            }
                        }
                    }
                }
                ccc.setEstado(Valores.CtaCteEstado.ANULADA.getId());
                em.merge(ccc);
            } else if (facturaVenta.getFormaPago() == Valores.FormaPago.CHEQUE.getId()) {
                throw new UnsupportedOperationException("Anulación de Facturas, FormaPago = CHEQUE no implementada aún");
            } else if (facturaVenta.getFormaPago() == Valores.FormaPago.CONTADO_CHEQUE.getId()) {
                throw new UnsupportedOperationException("Anulación de Facturas, FormaPago = CONTADO/CHEQUE no implementada aún");
            }

            facturaVenta.setAnulada(true);
            em.merge(facturaVenta);
            //re-estableciendo stock
            List<DetalleVenta> itemList = facturaVenta.getDetallesVentaList();
            ProductoController productoCtrl = new ProductoController();
            StockJpaController stockCtrl = new StockJpaController();
            for (DetalleVenta detallesVenta : itemList) {
                stockCtrl.modificarStockBySucursal(detallesVenta.getProducto(), facturaVenta.getSucursal(), detallesVenta.getCantidad());
                productoCtrl.updateStockActual(detallesVenta.getProducto(), detallesVenta.getCantidad());
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Realiza todos los procesos de anulación de una FacturaCompra <html><ul>
     * <li> Asentar el reintegro del importe de facturaVenta a la
     * cajaMovimientoDestino. <li> Re-establece los stock de Productos
     * involucrados. <li> Si es por CtaCte, ccc.estado = 3 (anulado), así como
     * los posible pagos realizados. </ul></html>
     *
     * @param facturaCompra
     * @param cajaMovimientoDestino
     * @throws Exception
     */
    public void anular(FacturaCompra facturaCompra, CajaMovimientos cajaMovimientoDestino) throws Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            facturaCompra = em.find(FacturaCompra.class, facturaCompra.getId());
            cajaMovimientoDestino = em.find(CajaMovimientos.class, cajaMovimientoDestino.getId());
            DetalleCajaMovimientos newDetalleCajaMovimiento;
            //si fue al CONTADO...
            if (facturaCompra.getFormaPago() == Valores.FormaPago.CONTADO.getId()) {
                newDetalleCajaMovimiento = new DetalleCajaMovimientos();
                newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoDestino);
                newDetalleCajaMovimiento.setIngreso(true);
                newDetalleCajaMovimiento.setMonto(facturaCompra.getImporte());
                newDetalleCajaMovimiento.setNumero(facturaCompra.getId());
                newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.ANULACION);
                newDetalleCajaMovimiento.setDescripcion("F" + facturaCompra.getTipo() + UTIL.AGREGAR_CEROS(facturaCompra.getNumero(), 12) + " [ANULADA]");
                newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
                new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
            } else if (facturaCompra.getFormaPago() == Valores.FormaPago.CTA_CTE.getId()) {
                // o CTA CTE..
                CtacteProveedor ccp = new CtacteProveedorJpaController().findCtacteProveedorByFactura(facturaCompra.getId());
                //si se hicieron REMESA's de pago de esta deuda
                if (ccp.getEntregado() > 0) {
                    List<Remesa> remesaList = new RemesaJpaController().findByFactura(facturaCompra);
                    boolean detalleUnico; //Therefore, the entire Recibo must be annulled
                    for (Remesa remesaQueEnSuDetalleContieneLaFactura : remesaList) {
                        detalleUnico = false;
                        if (remesaQueEnSuDetalleContieneLaFactura.getDetalleRemesaList().size() == 1) {
                            detalleUnico = true;
                        }
                        for (DetalleRemesa detalleRemesa : remesaQueEnSuDetalleContieneLaFactura.getDetalleRemesaList()) {
                            if (detalleRemesa.getFacturaCompra().equals(facturaCompra)) {
                                detalleRemesa.setObservacion("ANULADO - " + detalleRemesa.getObservacion());
                                detalleRemesa.setAnulado(true);
                                remesaQueEnSuDetalleContieneLaFactura.setMontoEntrega(remesaQueEnSuDetalleContieneLaFactura.getMonto() - detalleRemesa.getMontoEntrega());
                                newDetalleCajaMovimiento = new DetalleCajaMovimientos();
                                newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoDestino);
                                newDetalleCajaMovimiento.setIngreso(true);
                                newDetalleCajaMovimiento.setMonto(detalleRemesa.getMontoEntrega());
                                newDetalleCajaMovimiento.setNumero(facturaCompra.getId());
                                newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.ANULACION);
                                newDetalleCajaMovimiento.setDescripcion("F" + facturaCompra.getTipo()
                                        + UTIL.AGREGAR_CEROS(facturaCompra.getNumero(), 12)
                                        + " -> R" + remesaQueEnSuDetalleContieneLaFactura.getNumero() + " [ANULADA]");
                                newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
                                em.persist(newDetalleCajaMovimiento);
                                em.merge(detalleRemesa);
                                if (detalleUnico) {
                                    remesaQueEnSuDetalleContieneLaFactura.setEstado(false);
                                }
                                em.merge(remesaQueEnSuDetalleContieneLaFactura);
                            }
                        }
                    }
                }
                ccp.setEstado((short) 3);
                em.merge(ccp);
            }

            facturaCompra.setAnulada(true);
            em.merge(facturaCompra);
            //re-estableciendo stock
            List<DetalleCompra> itemList = facturaCompra.getDetalleCompraList();
            ProductoController productoCtrl = new ProductoController();
            StockJpaController stockCtrl = new StockJpaController();
            for (DetalleCompra detallesVenta : itemList) {
                //resta el stock
                stockCtrl.modificarStockBySucursal(detallesVenta.getProducto(), facturaCompra.getSucursal(), -detallesVenta.getCantidad());
                //actualiza esa variable de mierda que no se para que creé..
                productoCtrl.updateStockActual(detallesVenta.getProducto(), -detallesVenta.getCantidad());
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Busca la CajaMovimiento abierta correspodiente a la Caja candidata. Es
     * decir con fecha_cierre == NULL
     *
     * @param cajaCandidata
     * @return una entidad CajaMomiviento, o
     * <code>null</code> si no existe una para la cajaCandidata
     * @throws NoResultException
     * @throws NonUniqueResultException
     */
    public CajaMovimientos findCajaMovimientoAbierta(Caja cajaCandidata) throws NoResultException, NonUniqueResultException {
        EntityManager em = getEntityManager();
        CajaMovimientos cajaMovimiento = null;
        try {
            //busca la cajaMovimiento cuya fechaCierre != NULL (debería haber solo UNA)
            cajaMovimiento = (CajaMovimientos) em.createQuery("SELECT o FROM " + getEntityClass().getSimpleName() + " o"
                    + " where o.fechaCierre is null AND o.caja.id =" + cajaCandidata.getId()).setHint(QueryHints.REFRESH, true).getSingleResult();
        } catch (NoResultException ex) {
            //cuando Ruben hace su magia pasa esto!
            throw ex;
        } catch (NonUniqueResultException ex) {
            //esto ya sería el colmo...!!
            Logger.getLogger(this.getClass()).log(Level.FATAL, "HAY MAS DE 1 ABIERTA!!! -> CAJA: " + cajaCandidata, ex);
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return cajaMovimiento;
    }

    /**
     * Crea (abre) LA SIGUIENTE
     * <code>CajaMovimientos</code> implicita POST cierre de la actual. El
     * montoApertura de la nueva
     * <code>CajaMovimientos</code> es == al montoCierre de la anterior. La
     * fechaApertura de la nueva es == a un día después de la anterior.
     *
     * @param cajaMovimiento la que precede a la que se va abrir.
     */
    private void abrirNextCajaMovimiento(CajaMovimientos cajaMovimiento) throws Exception {
        CajaMovimientos cm = new CajaMovimientos();
        cm.setCaja(cajaMovimiento.getCaja());
        //fecha de apertura, un día después del cierre de ESTA
        cm.setFechaApertura(UTIL.customDateByDays(cajaMovimiento.getFechaCierre(), +1));
        cm.setMontoApertura(cajaMovimiento.getMontoCierre());
        cm.setSistemaFechaApertura(new Date());
        cm.setDetalleCajaMovimientosList(new ArrayList<DetalleCajaMovimientos>());
        //creando el 1er detalleCajaMovimiento..
        DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
        dcm.setDescripcion("Apertura de caja");
        dcm.setIngreso(true);
        dcm.setMonto(cm.getMontoApertura());
        dcm.setNumero(-1); //meaningless yet...
        dcm.setTipo(DetalleCajaMovimientosJpaController.APERTURA_CAJA);
        dcm.setUsuario(UsuarioJpaController.getCurrentUser());
        if (dcm.getMovimientoConcepto() == null) {
            //default value
            dcm.setMovimientoConcepto(MovimientoConceptoJpaController.EFECTIVO);
        }
        cm.getDetalleCajaMovimientosList().add(dcm);
        create(cm);
    }

    /**
     * Arma la descripción del detalleCajaMovimiento. Si se imprime factura ej:
     * F + [letra de factura] + [número de factura] Si es interno ej: I +
     * [número de movimento interno]
     *
     * @param factura
     * @return Un String con la descripción.
     */
    private String getDescripcion(FacturaVenta facturaVenta) {
        String codigoDescrip;
        if (facturaVenta.getNumero() == 0) {
            codigoDescrip = "I" + facturaVenta.getMovimientoInterno();
        } else {
            codigoDescrip = "F" + facturaVenta.getTipo()
                    + UTIL.AGREGAR_CEROS(String.valueOf(facturaVenta.getNumero()), 12);
        }
        return codigoDescrip;
    }

    public Integer findLastCajaMovimientoIDCerrada(Caja caja) {
        return (Integer) getEntityManager().createQuery("SELECT MAX(o.id) FROM " + getEntityClass().getSimpleName() + " o "
                + "WHERE o.caja.id = " + caja.getId()).getSingleResult();
    }

    public int getNextNumeroMovimientoCajaToCaja() {
        //se especifica o.ingreso = true .. porque cada movimiento Caja to Caja
        //genera 2 movimientos (un ingreso y un egreso) ... lo cual duplica la cantidad real
        Object o = getEntityManager().createQuery("SELECT COUNT(o.id) FROM DetalleCajaMovimientos o "
                + " WHERE o.ingreso = true AND o.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA).getSingleResult();
        if (o == null) {
            return 1;
        } else {
            return 1 + Integer.valueOf(o.toString());
        }
    }
}
